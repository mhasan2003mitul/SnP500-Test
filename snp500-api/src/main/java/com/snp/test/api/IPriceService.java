package com.snp.test.api;

import java.util.List;

public interface IPriceService {

  void startPriceService(String priceServiceName);
  void registerPriceServiceProducer(String producerName);
  void registerPriceServiceConsumer(String producerName, String consumerName);
  void producePriceData(String producerName, int batchId, int chunkSize, List<PriceData> priceDataList);
  void consumePriceData(String consumerName);
  void stopProducingPriceData(String producerName, int batchId);
  void shutdown();

}
