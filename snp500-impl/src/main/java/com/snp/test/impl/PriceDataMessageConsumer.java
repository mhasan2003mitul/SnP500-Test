package com.snp.test.impl;

import com.snp.test.api.PriceDataMessage;
import com.snp.test.api.ReceiveMessageProvider;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(staticName = "of")
@Setter
@Getter
class PriceDataMessageConsumer implements ReceiveMessageProvider {
  @NonNull
  final private int batchId;
  private volatile boolean isCanceled;
  private volatile boolean isCompleted;
  @NonNull
  final private AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel;
  final private Map<String, Integer> instrumentLastPrice = new ConcurrentHashMap<>();
  @Override
  public <T> T receive(Class<T> type) {
    try {
      // Consumer will stop reading data once CANCEL or COMPLETE message is send.
      while (!isCanceled && !isCompleted) {
        PriceDataMessage priceDataMessage = priceDataMessageChannel.get(batchId).poll();
        if (priceDataMessage != null) {
          priceDataMessage.getPriceDataList().stream().forEach(priceData -> {
            instrumentLastPrice.put(priceData.getId(), priceData.getPrice());
          });
          System.out.println("Received Message: "+ priceDataMessage);
          Thread.sleep(1000);
        }

        if(isCanceled) {
          instrumentLastPrice.clear();
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return type.cast(instrumentLastPrice);
  }
}
