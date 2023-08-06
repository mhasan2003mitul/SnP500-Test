package com.snp.test;

import com.snp.test.api.PriceData;
import com.snp.test.impl.PriceService;
import java.util.Arrays;
import java.util.Random;

public class SnP500APP {
  public static void main(String[] args) {
    final Random random = new Random(100);
    final PriceData[] priceData1 = new PriceData[10];
    priceData1[0] = new PriceData("A", System.currentTimeMillis(), random.nextInt(100));
    priceData1[1] = new PriceData("B", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[2] = new PriceData("C", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[3] = new PriceData("D", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[4] = new PriceData("E", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[5] = new PriceData("F", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[6] = new PriceData("G", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[7] = new PriceData("H", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[8] = new PriceData("I", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData1[9] = new PriceData("J", System.currentTimeMillis() + 1, random.nextInt(100));

    PriceService priceService = new PriceService();
    priceService.startPriceService("SnP");
    priceService.registerPriceServiceProducer("NYSE");
    priceService.registerPriceServiceConsumer("NYSE", "DEGIRO");
    priceService.producePriceData("NYSE", 1, 2, Arrays.asList(priceData1));
    priceService.consumePriceData("DEGIRO");

    final PriceData[] priceData2 = new PriceData[10];
    priceData2[0] = new PriceData("Z", System.currentTimeMillis(), random.nextInt(100));
    priceData2[1] = new PriceData("Y", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[2] = new PriceData("X", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[3] = new PriceData("W", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[4] = new PriceData("V", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[5] = new PriceData("U", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[6] = new PriceData("T", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[7] = new PriceData("S", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[8] = new PriceData("R", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[9] = new PriceData("Q", System.currentTimeMillis() + 1, random.nextInt(100));

    priceService.registerPriceServiceProducer("AMS");
    priceService.registerPriceServiceConsumer("AMS", "ETORO");
    priceService.producePriceData("AMS", 1, 3, Arrays.asList(priceData2));
    priceService.consumePriceData("ETORO");

    final PriceData[] priceData3 = new PriceData[10];
    priceData3[0] = new PriceData("A", System.currentTimeMillis(), random.nextInt(100));
    priceData3[1] = new PriceData("Z", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[2] = new PriceData("B", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[3] = new PriceData("Y", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[4] = new PriceData("C", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[5] = new PriceData("X", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[6] = new PriceData("D", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[7] = new PriceData("W", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[8] = new PriceData("E", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData3[9] = new PriceData("V", System.currentTimeMillis() + 1, random.nextInt(100));

    priceService.registerPriceServiceProducer("CMX");
    priceService.registerPriceServiceConsumer("CMX", "RABO");
    priceService.producePriceData("CMX", 1, 4, Arrays.asList(priceData3));
    priceService.consumePriceData("RABO");

    priceService.stopProducingPriceData("NYSE", 1);

    Runtime.getRuntime().addShutdownHook(new Thread(()-> {
      priceService.shutdown();
      System.out.println("Close Application.");
    }));

  }
}