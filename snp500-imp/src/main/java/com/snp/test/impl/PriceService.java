package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.IPriceService;
import com.snp.test.api.PriceData;
import com.snp.test.api.PriceDataMessage;
import java.util.AbstractMap;
import java.util.Arrays;
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
  private final Map<String, PriceDataMessageConsumer> priceDataConsumerMap = new ConcurrentHashMap<>();
  private final Map<String, String> consumerConsumeToProducer = new ConcurrentHashMap<>();
  private String priceServiceName;

  final private Map<String, Integer> instrumentLastPrice = new ConcurrentHashMap<>();

  public void startPriceService(String priceServiceName) {
      this.priceServiceName = priceServiceName;
  }

  public void registerPriceServiceProducer(String producerName) {
    controlMessageChannels.putIfAbsent(producerName, new LinkedBlockingQueue<>());
    priceDataMessageChannels.putIfAbsent(producerName, new ConcurrentHashMap<>());
  }

  public void registerPriceServiceConsumer(String producerName, String consumerName) {
    consumerConsumeToProducer.put(consumerName, producerName);
  }

  public void producePriceData(String producerName, int batchId, int chunkSize, PriceData[] priceData) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessageChannels.get(producerName);
    final AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = priceDataMessageChannels.get(producerName);

    executorService.submit(()->{
      // Send Start Batch Message
      StartMessageProducer.of(batchId, priceData.length, controlMessageChannel).send();
      executorService.submit(()->{
        // Send Price Data Message
        PriceDataMessageProducer.of(batchId, chunkSize, Arrays.asList(priceData), priceDataMessageChannel).send();
        // Send Batch Complete Message
        CompleteMessageProducer.of(batchId, priceData.length, controlMessageChannel).send();
      });
    });
  }

  public void consumePriceData(String consumerName) {
    final String producerName = consumerConsumeToProducer.get(consumerName);
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessageChannels.get(producerName);
    final AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = priceDataMessageChannels.get(producerName);

    executorService.submit(()->{
      while(true) {
        // Read producer control message
        ControlMessageConsumer controlMessageConsumer = ControlMessageConsumer.of(controlMessageChannel);

        while(true) {
          // Read control message from control channel
          ControlMessage controlMessage = controlMessageConsumer.receive(ControlMessage.class);
          System.out.println("" + controlMessage);
          switch (controlMessage.getCommandType()) {
            case START_BATCH_COMMAND:
              // Start reading price data from the price data channel
              executorService.submit(()->{
                PriceDataMessageConsumer priceDataConsumer = PriceDataMessageConsumer.of(controlMessage.getBatchId(), false, priceDataMessageChannel);
                priceDataConsumerMap.put(consumerName, priceDataConsumer);
                // Receive Price Data
                priceDataConsumer.receive(List.class);
              });
              break;
            case CANCEL_BATCH_COMMAND:
            case COMPLETE_BATCH_COMMAND:
              System.out.println("Price List: " + priceDataConsumerMap.get(consumerName).getInstrumentLastPrice());
              priceDataConsumerMap.get(consumerName).setStopped(Boolean.TRUE);
              break;
            default:
              System.out.println("Not a valid command");
          }
        }
      }
    });
  }

  public void stopProducingPriceData(String producerName, int batchId) {
    final BlockingQueue<ControlMessage> controlMessageChannel = controlMessageChannels.get(producerName);
    executorService.submit(()->{
      try {
        Thread.sleep(1000);
        CancelMessageProducer.of(batchId, controlMessageChannel).send();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
