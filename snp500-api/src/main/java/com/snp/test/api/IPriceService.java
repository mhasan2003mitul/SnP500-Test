package com.snp.test.api;

import java.util.List;
import java.util.Map;

/**
 * Define contract for a price service. It can support multi price data producer price data upload
 * and multi consumer price data read.
 */
public interface IPriceService {

  /**
   * Start a price service.
   * @param priceServiceName Price Service Name
   */
  void startPriceService(String priceServiceName);

  /**
   * Register a price data producer for a price service. Normally exchanges like NYSE, AMS, CMX, etc.
   * are te producer of the price service.
   * @param producerName name of the producer.
   */
  void registerPriceServiceProducer(String producerName);

  /**
   * Register a price data consumer for a price data producer. Any one can be a price data consumer
   * like trading application, newspaper, tv media etc. A consumer can read data from multiple price
   * data producer.
   * @param producerName
   * @param consumerName
   */
  void registerPriceServiceConsumerForAProducer(String producerName, String consumerName);

  /**
   * Start price data upload for a pre-registered consumer.
   * @param producerName name of the price data producer
   * @param consumerName name of the price data consumer
   * @param batchId price data batch id
   * @param chunkSize size of chunk
   * @param priceDataList list of price data list for upload
   */
  void producePriceDataForAConsumer(String producerName, String consumerName, int batchId, int chunkSize, List<PriceData> priceDataList);

  /**
   * Consumer read data from a pre-registered QUEUE.
   * @param consumerName name of the consumer
   * @param producerName name of the producer
   */
  void consumePriceDataFromAProducer(String consumerName, String producerName);

  /**
   * Cancel price data upload for a consumer.
   * @param producerName name of the producer.
   * @param consumerName name of the consumer.
   * @param batchId batch id of the canceled upload.
   */
  void stopProducingPriceDataForAConsumer(String producerName, String consumerName, int batchId);

  /**
   * Get last price data from a consumer that already read out from the producer QUEUE.
   * @param consumerName name of a consumer.
   * @param producerName name of a producer.
   * @return map of instrument and price
   */
  Map<String, Integer> getLastPriceDataFromAConsumer(String consumerName, String producerName);

  /**
   * Get price data of a instrument
   * @param consumerName consumer name
   * @param producerName producer name
   * @param batchId batch id
   * @param instrumentId id of the instrument
   */
  void requestInstrumentPriceData(String consumerName, String producerName, int batchId, String instrumentId);
  /**
   * Stop price service gracefully.
   */
  void shutdown();
}
