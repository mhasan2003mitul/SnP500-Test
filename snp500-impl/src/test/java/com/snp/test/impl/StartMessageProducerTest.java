package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

class StartMessageProducerTest {

  @Test
  void send() {
    StartMessageProducer startMessageProducer = StartMessageProducer.of(TestDataUtil.BATCH_ID, TestDataUtil.NUMBER_OF_PRICE_DATA, new LinkedBlockingQueue<>());
    assertEquals(Boolean.TRUE, startMessageProducer.send());
  }
}