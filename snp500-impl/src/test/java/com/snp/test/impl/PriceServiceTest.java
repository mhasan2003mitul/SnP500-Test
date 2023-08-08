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
}