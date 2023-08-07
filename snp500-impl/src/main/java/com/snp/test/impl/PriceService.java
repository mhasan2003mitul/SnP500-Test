package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.IPriceService;
import com.snp.test.api.PriceData;
import com.snp.test.api.PriceDataMessage;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PriceService implements IPriceService {
  private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private final Map<String, BlockingQueue<ControlMessage>> controlMessageChannels = new ConcurrentHashMap<>();
  private final Map<String, AbstractMap<Integer, BlockingQueue<PriceDataMessage>>> priceDataMessageChannels = new ConcurrentHashMap<>();
  private final Map<String, PriceDataMessageProducer> priceDataProducerMap = new ConcurrentHashMap<>();
  private final Map<String, PriceDataMessageConsumer> priceDataConsumerMap = new ConcurrentHashMap<>();
  private final Map<String, String> consumerConsumeToProducer = new ConcurrentHashMap<>();
  private String priceServiceName;

  @Override
  public void startPriceService(String priceServiceName) {
      this.priceServiceName = priceServiceName;
  }

  @Override
  public void registerPriceServiceProducer(String producerName) {
    controlMessageChannels.putIfAbsent(producerName, new LinkedBlockingQueue<>());
    priceDataMessageChannels.putIfAbsent(producerName, new ConcurrentHashMap<>());
  }

  @Override
  public void registerPriceServiceConsumer(String producerName, String consumerName) {
    consumerConsumeToProducer.put(consumerName, producerName);
  }

  @Override
  public void producePriceData(String producerName, int batchId, int chunkSize, List<PriceData> priceDataList) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessageChannels.get(producerName);
    final AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = priceDataMessageChannels.get(producerName);

    executorService.submit(()->{
      // Send Start Batch Message
      StartMessageProducer.of(batchId, priceDataList.size(), controlMessageChannel).send();
      executorService.submit(()->{

        // Send Price Data Message
        PriceDataMessageProducer priceDataMessageProducer = PriceDataMessageProducer.of(batchId, chunkSize, priceDataList, priceDataMessageChannel);
        priceDataProducerMap.put(producerName, priceDataMessageProducer);
        priceDataMessageProducer.send();

        // Send Batch Complete Message
        if(!priceDataMessageProducer.isStopped()) {
          CompleteMessageProducer.of(batchId, priceDataList.size(), controlMessageChannel).send();
        }
      });
    });
  }

  @Override
  public void consumePriceData(String consumerName) {
    final String producerName = consumerConsumeToProducer.get(consumerName);
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessageChannels.get(producerName);
    final AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = priceDataMessageChannels.get(producerName);

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
              priceDataConsumerMap.put(consumerName, priceDataConsumer);
              // Receive Price Data
              priceDataConsumer.receive(Map.class);
            });
            break;
          case CANCEL_BATCH_COMMAND:
            // Notify producer has canceled a price data batch.
            priceDataConsumerMap.get(consumerName).setCanceled(Boolean.TRUE);
            System.out.printf("Price data batch %s has been canceled for %s from %s.%n", controlMessage.getBatchId(), consumerName, consumerConsumeToProducer.get(consumerName));
            break;
          case COMPLETE_BATCH_COMMAND:
            // Notify producer has completed a price data batch.
            priceDataConsumerMap.get(consumerName).setCompleted(Boolean.TRUE);
            System.out.printf("Price List for %s from %s: %s %n", consumerName, consumerConsumeToProducer.get(consumerName),  priceDataConsumerMap.get(consumerName).getInstrumentLastPrice().toString());
            break;
          default:
            System.out.println("Not a valid command message");
        }
      }
    });
  }

  @Override
  public void stopProducingPriceData(String producerName, int batchId) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessageChannels.get(producerName);
    executorService.submit(()->{
      try {
        priceDataProducerMap.get(producerName).setStopped(Boolean.TRUE);
        Thread.sleep(1000);
        CancelMessageProducer.of(batchId, controlMessageChannel).send();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public Map<String, Integer> getLastPriceData(String consumerName) {
    return priceDataConsumerMap.get(consumerName).getInstrumentLastPrice();
  }

  @Override
  public void shutdown() {
    executorService.shutdown();
    System.out.println("Shutdown " + priceServiceName + " price service.");
  }
}
