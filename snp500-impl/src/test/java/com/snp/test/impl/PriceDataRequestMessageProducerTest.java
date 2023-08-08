package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

class PriceDataRequestMessageProducerTest {

  @Test
  void send() {
    PriceDataRequestMessageProducer priceDataRequestMessageProducer = PriceDataRequestMessageProducer.of(TestDataUtil.BATCH_ID, TestDataUtil.INSTRUMENT_ID, new LinkedBlockingQueue<>());
    assertEquals(Boolean.TRUE, priceDataRequestMessageProducer.send());
  }
}