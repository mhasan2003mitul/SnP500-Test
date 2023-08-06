package com.snp.test;

import com.snp.test.api.PriceDataMessage;
import com.snp.test.api.ControlMessage;
import com.snp.test.api.PriceData;
import com.snp.test.impl.PriceDataMessageConsumer;
import com.snp.test.impl.PriceService;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class SnP500APP {
  public static void main(String[] args) {
    final Map<Integer, PriceDataMessageConsumer> activeConsumer = new ConcurrentHashMap<>();
    final Map<String, Integer> instrumentLastPrice = new HashMap<>();
    final Random random = new Random(100);
    final PriceData priceData1[] = new PriceData[10];
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

    final PriceData priceData2[] = new PriceData[10];
    priceData2[0] = new PriceData("Z", System.currentTimeMillis(), random.nextInt(100));
    priceData2[1] = new PriceData("Y", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[2] = new PriceData("X", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[3] = new PriceData("S", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[4] = new PriceData("M", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[5] = new PriceData("N", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[6] = new PriceData("O", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[7] = new PriceData("P", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[8] = new PriceData("Q", System.currentTimeMillis() + 1, random.nextInt(100));
    priceData2[9] = new PriceData("R", System.currentTimeMillis() + 1, random.nextInt(100));

    Runtime.getRuntime().addShutdownHook(new Thread(()-> {
      executorService.shutdown();
      System.out.println("Close Application.");
    }));

    PriceService priceService = new PriceService();
    priceService.startPriceService("SnP");
    priceService.registerPriceServiceProducer("NYSE");
    priceService.registerPriceServiceConsumer("NYSE", "DEGIRO");
    priceService.producePriceData("NYSE", 1, 2, priceData1);
    priceService.consumePriceData("DEGIRO");

    priceService.registerPriceServiceProducer("AMS");
    priceService.registerPriceServiceConsumer("AMS", "ETORO");
    priceService.producePriceData("AMS", 1, 3, priceData2);
    priceService.consumePriceData("ETORO");

    priceService.registerPriceServiceProducer("CMX");
    priceService.registerPriceServiceConsumer("CMX", "RABO");
    priceService.producePriceData("CMX", 1, 4, priceData2);
    priceService.consumePriceData("RABO");

    priceService.stopProducingPriceData("NYSE", 1);
  }
}