package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.snp.test.api.PriceData;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PriceServiceTest {

  @Test
  void getLastPriceDataFromAConsumer() {

    final List<PriceData> priceDataList = TestDataUtil.getPriceData();

    PriceService priceService = new PriceService();
    priceService.startPriceService("SnP");

    // Get price data for DEGIRO from NYSE
    priceService.registerPriceServiceProducer("NYSE");
    priceService.registerPriceServiceConsumerForAProducer("NYSE", "DEGIRO");
    priceService.producePriceDataForAConsumer("NYSE", "DEGIRO",1, 2, priceDataList);
    priceService.consumePriceDataFromAProducer("DEGIRO", "NYSE");

    assertEquals(priceDataList.stream().collect(Collectors.toMap(PriceData::getId, PriceData::getPrice)), priceService.getLastPriceDataFromAConsumer("DEGIRO", "NYSE"));
  }

  @Test
  void requestInstrumentPriceData() throws InterruptedException {
    final List<PriceData> priceDataList = TestDataUtil.getPriceData();

    PriceService priceService = new PriceService();
    priceService.startPriceService("SnP");

    // Get price data for DEGIRO from NYSE
    priceService.registerPriceServiceProducer("NYSE");
    priceService.registerPriceServiceConsumerForAProducer("NYSE", "DEGIRO");
    priceService.producePriceDataForAConsumer("NYSE", "DEGIRO",1, 2, priceDataList);
    priceService.consumePriceDataFromAProducer("DEGIRO", "NYSE");

    // Get price data for ETORO from NYSE
    priceService.registerPriceServiceConsumerForAProducer("NYSE", "ETORO");
    priceService.producePriceDataForAConsumer("NYSE", "ETORO" ,1, 2, priceDataList);
    priceService.consumePriceDataFromAProducer("ETORO", "NYSE");

    // NYSE cancel price data for DEGIRO
    priceService.stopProducingPriceDataForAConsumer("NYSE", "DEGIRO", 1);

    System.out.println("Last Price from NYSE for DEGIRO: "+priceService.getLastPriceDataFromAConsumer("DEGIRO", "NYSE"));
    System.out.println("Last Price from NYSE for ETORO: "+priceService.getLastPriceDataFromAConsumer("ETORO", "NYSE"));

    priceService.requestInstrumentPriceData("DEGIRO", "NYSE", 2,"A");

    Thread.sleep(1000);
    System.out.println("Last Price: "+priceService.getLastPriceDataFromAConsumer("DEGIRO", "NYSE"));

//    assertEquals(priceDataList.stream().collect(Collectors.toMap(PriceData::getId, PriceData::getPrice)), priceService.getLastPriceDataFromAConsumer("DEGIRO", "NYSE"));
  }
}