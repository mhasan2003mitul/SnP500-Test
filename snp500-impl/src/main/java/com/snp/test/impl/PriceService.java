package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.IPriceService;
import com.snp.test.api.PriceData;
import com.snp.test.api.PriceDataMessage;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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

  @Override
  public void consumePriceDataFromAProducer(String consumerName, String producerName) {
    consumePriceDataFromProducer(consumerName, producerName);
  }

  private void consumePriceDataFromProducer(String consumerName, String producerName) {
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
            // Start reading price data from the price data channel
            executorService.submit(()->{
              PriceDataMessageConsumer priceDataConsumer = PriceDataMessageConsumer.of(controlMessage.getBatchId(),  priceDataMessageChannel);
              priceDataConsumerMap.get(consumerName).put(producerName, priceDataConsumer);
              // Receive Price Data
              priceDataConsumer.receive(Map.class);

              // Print Price Data if it receives successfully
              if(priceDataConsumer.isCanceled()) {
                System.out.printf("Price data batch %s has been canceled for %s from %s.%n", controlMessage.getBatchId(), consumerName, producerName);
              } else {
                System.out.printf("Price data list for %s from %s: %s %n", consumerName, producerName,  priceDataConsumerMap.get(consumerName).get(producerName).getInstrumentLastPrice().toString());
              }
            });
            break;
          case CANCEL_BATCH_COMMAND:
            // Notify producer has canceled a price data batch.
            executorService.submit(()->{
              priceDataConsumerMap.get(consumerName).get(producerName).setCanceled(Boolean.TRUE);
            });
            break;
          case COMPLETE_BATCH_COMMAND:
            // Notify producer has completed a price data batch.
            executorService.submit(()->{
              System.out.println(String.format("Notify %s --> %s has finished sending price data.", consumerName, producerName));
              priceDataConsumerMap.get(consumerName).get(producerName).setCompleted(Boolean.TRUE);
            });
            break;
          default:
            System.out.println("Not a valid command message");
        }
      }
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

  public Map<String, Integer> getLastPriceData(String consumerName, String producerName) {
    return priceDataConsumerMap.get(consumerName).get(producerName).getInstrumentLastPrice();
  }

  @Override
  public void shutdown() {
    executorService.shutdown();
    System.out.println("Shutdown " + priceServiceName + " price service.");
  }
}
