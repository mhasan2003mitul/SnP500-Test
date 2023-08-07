package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.snp.test.api.PriceData;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriceDataMessageProducerTest {

  @Test
  void send() {
    PriceDataMessageProducer priceDataMessageProducer = PriceDataMessageProducer.of(TestDataUtil.BATCH_ID, TestDataUtil.CHUNK_SIZE, TestDataUtil.getPriceData(), new ConcurrentHashMap<>());
    assertEquals(Boolean.TRUE, priceDataMessageProducer.send());
  }
}