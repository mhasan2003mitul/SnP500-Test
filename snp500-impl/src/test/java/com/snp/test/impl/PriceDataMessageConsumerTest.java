package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.snp.test.api.PriceData;
import com.snp.test.api.PriceDataMessage;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PriceDataMessageConsumerTest {
  @Test
  void receive() throws InterruptedException {

    AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel = new ConcurrentHashMap<>();
    PriceDataMessageProducer priceDataMessageProducer = PriceDataMessageProducer.of(TestDataUtil.BATCH_ID, TestDataUtil.CHUNK_SIZE, TestDataUtil.getPriceData(), priceDataMessageChannel);
    assertEquals(Boolean.TRUE, priceDataMessageProducer.send());

    Map<String, Integer> instrumentPrice = new ConcurrentHashMap<>();

    PriceDataMessageConsumer priceDataMessageConsumer = PriceDataMessageConsumer.of(TestDataUtil.BATCH_ID,priceDataMessageChannel);

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    executorService.submit(()->{
      instrumentPrice.putAll(priceDataMessageConsumer.receive(Map.class));
      latch.countDown();
    });

    executorService.submit(()->{
      try {
        Thread.sleep(10000);
        latch.countDown();
        priceDataMessageConsumer.setCompleted(Boolean.TRUE);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    latch.await();
    assertNotNull(instrumentPrice);
    assertEquals(TestDataUtil.getPriceData().size(), instrumentPrice.size());
    assertEquals(TestDataUtil.getPriceData().stream().collect(Collectors.toMap(PriceData::getId, PriceData::getPrice)), instrumentPrice);
  }
}