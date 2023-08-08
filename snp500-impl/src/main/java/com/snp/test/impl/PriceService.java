package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.IPriceService;
import com.snp.test.api.PriceData;
import com.snp.test.api.PriceDataMessage;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PriceService implements IPriceService {
  private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private final Map<String, BlockingQueue<ControlMessage>> controlMessageChannels = new ConcurrentHashMap<>();
  private final Map<String, Map<String, BlockingQueue<ControlMessage>>> controlMessages = new ConcurrentHashMap<>();
  private final Map<String, AbstractMap<String, AbstractMap<Integer, BlockingQueue<PriceDataMessage>>>> priceDataMessageChannels = new ConcurrentHashMap<>();
  private final Map<String, Map<String, PriceDataMessageProducer>> priceDataProducerMap = new ConcurrentHashMap<>();
  private final Map<String, Map<String, PriceDataMessageConsumer>> priceDataConsumerMap = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> consumerConsumeToProducer = new ConcurrentHashMap<>();
  private final Map<String, List<PriceData>> producerToPriceDataMap = new ConcurrentHashMap<>();
  private String priceServiceName;

  @Override
  public void startPriceService(String priceServiceName) {
      this.priceServiceName = priceServiceName;
  }

  @Override
  public void registerPriceServiceProducer(String producerName) {
    controlMessageChannels.putIfAbsent(producerName, new LinkedBlockingQueue<>());
    controlMessages.putIfAbsent(producerName, new ConcurrentHashMap<>());
    priceDataMessageChannels.putIfAbsent(producerName, new ConcurrentHashMap<>());

  }

  @Override
  public void registerPriceServiceConsumerForAProducer(String producerName, String consumerName) {
    consumerConsumeToProducer.putIfAbsent(consumerName, new HashSet<>());
    consumerConsumeToProducer.get(consumerName).add(producerName);
    controlMessages.get(producerName).putIfAbsent(consumerName,new LinkedBlockingQueue<>());
    priceDataMessageChannels.get(producerName).putIfAbsent(consumerName, new ConcurrentHashMap<>());
    priceDataConsumerMap.put(consumerName, new ConcurrentHashMap<>());
  }

  @Override
  public void producePriceDataForAConsumer(String producerName, String consumerName, int batchId, int chunkSize, List<PriceData> priceDataList) {
    producerToPriceDataMap.put(producerName, priceDataList);
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessages.get(producerName).get(consumerName);
    final AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = priceDataMessageChannels.get(producerName).get(consumerName);

    executorService.submit(()->{
      try {
        // Send Start Batch Message
        System.out.println(producerName + " send start batch command to " + consumerName);
        StartMessageProducer.of(batchId, priceDataList.size(), controlMessageChannel).send();
        executorService.submit(()->{

          System.out.println(producerName + " sending price data to " + consumerName);
          // Send Price Data Message
          PriceDataMessageProducer priceDataMessageProducer = PriceDataMessageProducer.of(batchId, chunkSize, priceDataList, priceDataMessageChannel);
          priceDataProducerMap.putIfAbsent(producerName, new ConcurrentHashMap<>());
          priceDataProducerMap.get(producerName).put(consumerName, priceDataMessageProducer);
          priceDataMessageProducer.send();

          // Send Batch Complete Message
          if(!priceDataMessageProducer.isStopped()) {
            CompleteMessageProducer.of(batchId, priceDataList.size(), controlMessageChannel).send();
          }
        });
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  public void consumePriceDataFromAProducer(String consumerName, String producerName) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessages.get(producerName).get(consumerName);
    final AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = priceDataMessageChannels.get(producerName).get(consumerName);

    executorService.submit(()->{
      // Read producer control message
      ControlMessageConsumer controlMessageConsumer = ControlMessageConsumer.of(controlMessageChannel);

      while(true) {
        // Read control message from control channel
        ControlMessage controlMessage = controlMessageConsumer.receive(ControlMessage.class);
        System.out.println("Received: " + controlMessage);
        switch (controlMessage.getCommandType()) {
          case START_BATCH_COMMAND:
            processStartBatchCommand(consumerName, producerName, priceDataMessageChannel, controlMessage);
            break;
          case CANCEL_BATCH_COMMAND:
            processCancelBatchCommand(consumerName, producerName);
            break;
          case COMPLETE_BATCH_COMMAND:
            processCompleteBatchCommand(consumerName, producerName);
            break;
          case REQUEST_INSTRUMENT_PRICE_DATA_COMMAND:
            processRequestInstrumentPriceDataCommand(consumerName, producerName, controlMessage);
            break;
          default:
            System.out.println("Not a valid command message");
        }
      }
    });
  }

  private void processStartBatchCommand(String consumerName, String producerName,
      AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel,
      ControlMessage controlMessage) {
    // Start reading price data from the price data channel
    executorService.submit(()->{

      try {
        PriceDataMessageConsumer priceDataConsumer = priceDataConsumerMap.get(consumerName).get(producerName);

        if (priceDataConsumer == null) {
          priceDataConsumer = PriceDataMessageConsumer.of(controlMessage.getBatchId(), priceDataMessageChannel);
          priceDataConsumerMap.get(consumerName).put(producerName, priceDataConsumer);
        }

        // Receive Price Data
        priceDataConsumer.reset();
        priceDataConsumer.receive(Map.class);

        // Print Price Data if it receives successfully
        if(priceDataConsumer.isDataSendingCanceled()) {
          System.out.printf("Price data batch %s has been canceled for %s from %s.%n", controlMessage.getBatchId(),
              consumerName, producerName);
        } else {
          System.out.printf("Price data list for %s from %s: %s %n", consumerName, producerName,  priceDataConsumerMap.get(
              consumerName).get(producerName).getLastPriceData().toString());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  private void processCancelBatchCommand(String consumerName, String producerName) {
    // Notify producer has canceled a price data batch.
    executorService.submit(()->{
      priceDataConsumerMap.get(consumerName).get(producerName).setDataSendingCanceled(Boolean.TRUE);
      priceDataProducerMap.get(producerName).remove(consumerName);
    });
  }

  private void processCompleteBatchCommand(String consumerName, String producerName) {
    // Notify producer has completed a price data batch.
    executorService.submit(()->{
      System.out.println(String.format("Notify %s --> %s has finished sending price data.",
          consumerName, producerName));
      priceDataConsumerMap.get(consumerName).get(producerName).setDataSendingCompleted(Boolean.TRUE);
    });
  }

  private void processRequestInstrumentPriceDataCommand(String consumerName, String producerName, ControlMessage controlMessage) {
    executorService.submit(()->{
      String instrumentId = controlMessage.getInstrumentId();
      PriceData priceData = producerToPriceDataMap.get(producerName).stream().filter(pData -> pData.getId().equalsIgnoreCase(instrumentId)).findFirst().get();
      producePriceDataForAConsumer(producerName, consumerName, controlMessage.getBatchId(), 1, Arrays.asList(priceData));
    });
  }

  @Override
  public void stopProducingPriceDataForAConsumer(String producerName, String consumerName, int batchId) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessages.get(producerName).get(consumerName);
    executorService.submit(()->{
      try {
        Thread.sleep(1000);
        System.out.println(producerName + " send price data cancel for " + consumerName);
        priceDataProducerMap.get(producerName).get(consumerName).setStopped(Boolean.TRUE);
        CancelMessageProducer.of(batchId, controlMessageChannel).send();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  @Override
  public Map<String, Integer> getLastPriceDataFromAConsumer(String consumerName, String producerName) {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final Map<String, Integer> lastPriceData = new ConcurrentHashMap<>();
    try {
      executorService.submit(()->{
        PriceDataMessageConsumer priceDataMessageConsumer = null;
        while(true) {
          try {
            if(priceDataMessageConsumer == null || priceDataMessageConsumer.isDataSendingCanceled()
                || !priceDataConsumerMap.containsKey(consumerName) || !priceDataConsumerMap.get(consumerName).containsKey(producerName)) {
              priceDataMessageConsumer = priceDataConsumerMap.get(consumerName).get(producerName);
              continue;
            }

            if(priceDataMessageConsumer.isDataSendingCompleted()) {
              lastPriceData.putAll(priceDataMessageConsumer.getLastPriceData());
              countDownLatch.countDown();
              return;
            }
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return lastPriceData;
  }

  @Override
  public void requestInstrumentPriceData(String consumerName, String producerName, int batchId, String instrumentId) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessages.get(producerName).get(consumerName);
    executorService.submit(() ->{
      PriceDataRequestMessageProducer.of(batchId, instrumentId, controlMessageChannel).send();
    });
  }

  @Override
  public void shutdown() {
    executorService.shutdown();
    System.out.println("Shutdown " + priceServiceName + " price service.");
  }
}
