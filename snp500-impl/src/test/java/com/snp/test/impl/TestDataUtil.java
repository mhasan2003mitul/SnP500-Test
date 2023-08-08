package com.snp.test.impl;

import com.snp.test.api.PriceData;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataUtil {
  static final int BATCH_ID = 1;
  static final int NUMBER_OF_PRICE_DATA = 10;
  static final int CHUNK_SIZE = 3;

  static final String INSTRUMENT_ID = "A";

  public static List<PriceData> getPriceData() {
    PriceData[] priceData = new PriceData[10];
    priceData[0] = new PriceData("A", System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(100));
    priceData[1] = new PriceData("B", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[2] = new PriceData("C", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[3] = new PriceData("D", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[4] = new PriceData("E", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[5] = new PriceData("F", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[6] = new PriceData("G", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[7] = new PriceData("H", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[8] = new PriceData("I", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData[9] = new PriceData("J", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    return Arrays.asList(priceData);
  }

}
