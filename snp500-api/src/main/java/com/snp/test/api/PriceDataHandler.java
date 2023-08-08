package com.snp.test.api;

import java.util.Map;

public interface PriceDataHandler {

  /**
   * Get the map of all instrument price data consumed from the producer QUEUE.
   * @return map of instrument and price.
   */
  Map<String, Integer>  getLastPriceData();
}
