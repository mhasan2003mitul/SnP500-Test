package com.snp.test;

import com.snp.test.api.PriceData;
import com.snp.test.impl.PriceService;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SnP500APP {
  public static void main(String[] args) {

    final PriceData[] priceData1 = new PriceData[10];
    priceData1[0] = new PriceData("A", System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(100));
    priceData1[1] = new PriceData("B", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[2] = new PriceData("C", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[3] = new PriceData("D", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[4] = new PriceData("E", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[5] = new PriceData("F", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[6] = new PriceData("G", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[7] = new PriceData("H", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[8] = new PriceData("I", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData1[9] = new PriceData("J", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));

    final PriceData[] priceData2 = new PriceData[10];
    priceData2[0] = new PriceData("Z", System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(100));
    priceData2[1] = new PriceData("Y", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[2] = new PriceData("X", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[3] = new PriceData("W", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[4] = new PriceData("V", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[5] = new PriceData("U", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[6] = new PriceData("T", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[7] = new PriceData("S", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[8] = new PriceData("R", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData2[9] = new PriceData("Q", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));

    final PriceData[] priceData3 = new PriceData[10];
    priceData3[0] = new PriceData("A", System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(100));
    priceData3[1] = new PriceData("Z", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[2] = new PriceData("B", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[3] = new PriceData("Y", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[4] = new PriceData("C", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[5] = new PriceData("X", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[6] = new PriceData("D", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[7] = new PriceData("W", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[8] = new PriceData("E", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));
    priceData3[9] = new PriceData("V", System.currentTimeMillis() + 1, ThreadLocalRandom.current().nextInt(100));

    // Create and Price Service
    PriceService priceService = new PriceService();
    priceService.startPriceService("SnP");

    // Get price data for DEGIRO from NYSE
    priceService.registerPriceServiceProducer("NYSE");
    priceService.registerPriceServiceConsumerForAProducer("NYSE", "DEGIRO");
    priceService.producePriceDataForAConsumer("NYSE", "DEGIRO",1, 2, Arrays.asList(priceData1));
    priceService.consumePriceDataFromAProducer("DEGIRO", "NYSE");

    // Get price data for ETORO from NYSE
    priceService.registerPriceServiceConsumerForAProducer("NYSE", "ETORO");
    priceService.producePriceDataForAConsumer("NYSE", "ETORO" ,1, 2, Arrays.asList(priceData1));
    priceService.consumePriceDataFromAProducer("ETORO", "NYSE");

    // Get price data for ETORO from AMS
    priceService.registerPriceServiceProducer("AMS");
    priceService.registerPriceServiceConsumerForAProducer("AMS", "ETORO");
    priceService.producePriceDataForAConsumer("AMS", "ETORO",1, 3, Arrays.asList(priceData2));
    priceService.consumePriceDataFromAProducer("ETORO", "AMS");

    // NYSE cancel price data for DEGIRO
    priceService.stopProducingPriceDataForAConsumer("NYSE", "DEGIRO", 1);

    // Get price data for InterActiveBroker from AMS
    priceService.registerPriceServiceConsumerForAProducer("AMS", "InterActiveBroker");
    priceService.producePriceDataForAConsumer("AMS", "InterActiveBroker",1, 3, Arrays.asList(priceData2));
    priceService.consumePriceDataFromAProducer("InterActiveBroker", "AMS");

    // Get price data for InterActiveBroker from CMX
    priceService.registerPriceServiceProducer("CMX");
    priceService.registerPriceServiceConsumerForAProducer("CMX", "InterActiveBroker");
    priceService.producePriceDataForAConsumer("CMX", "InterActiveBroker",1, 4, Arrays.asList(priceData3));
    priceService.consumePriceDataFromAProducer("InterActiveBroker", "CMX");

    System.out.println("Last Price: "+priceService.getLastPriceDataFromAConsumer("DEGIRO", "NYSE"));
    System.out.println("Last Price: "+priceService.getLastPriceDataFromAConsumer("ETORO", "NYSE"));
    System.out.println("Last Price: "+priceService.getLastPriceDataFromAConsumer("ETORO", "AMS"));
    System.out.println("Last Price: "+priceService.getLastPriceDataFromAConsumer("InterActiveBroker", "AMS"));
    System.out.println("Last Price: "+priceService.getLastPriceDataFromAConsumer("InterActiveBroker", "CMX"));

    Runtime.getRuntime().addShutdownHook(new Thread(()-> {
      priceService.shutdown();
      System.out.println("Close Application.");
    }));

  }
}