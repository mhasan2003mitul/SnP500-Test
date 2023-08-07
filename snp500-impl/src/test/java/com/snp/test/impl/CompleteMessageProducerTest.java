package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

class CompleteMessageProducerTest {
  @Test
  void send() {
    CompleteMessageProducer completeMessageProducer = CompleteMessageProducer.of(TestDataUtil.BATCH_ID, TestDataUtil.NUMBER_OF_PRICE_DATA, new LinkedBlockingQueue<>());
    assertEquals(Boolean.TRUE, completeMessageProducer.send());
  }
}