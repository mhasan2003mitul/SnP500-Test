package com.snp.test.api;

import java.util.List;
import java.util.Map;

public interface IPriceService {

  void startPriceService(String priceServiceName);
  void registerPriceServiceProducer(String producerName);
  void registerPriceServiceConsumerForAProducer(String producerName, String consumerName);
  void producePriceDataForAConsumer(String producerName, String consumerName, int batchId, int chunkSize, List<PriceData> priceDataList);
  void consumePriceDataFromAProducer(String consumerName, String producerName);
  void stopProducingPriceDataForAConsumer(String producerName, String consumerName, int batchId);
  Map<String, Integer> getLastPriceDataFromAConsumer(String consumerName, String producerName);
  void shutdown();

}
