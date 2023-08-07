package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

class CancelMessageProducerTest {

  @Test
  void send() {
    CancelMessageProducer cancelMessageProducer = CancelMessageProducer.of(TestDataUtil.BATCH_ID, new LinkedBlockingQueue<>());
    assertEquals(Boolean.TRUE, cancelMessageProducer.send());
  }
}