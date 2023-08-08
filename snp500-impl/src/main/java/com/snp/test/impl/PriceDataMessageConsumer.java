package com.snp.test.impl;

import com.snp.test.api.ConsumerResetHandler;
import com.snp.test.api.PriceDataHandler;
import com.snp.test.api.PriceDataMessage;
import com.snp.test.api.ReceiveMessageProvider;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(staticName = "of")
class PriceDataMessageConsumer implements ReceiveMessageProvider, PriceDataHandler,
    ConsumerResetHandler {
  @NonNull
  final private int batchId;
  @Getter
  @Setter
  private volatile boolean isDataSendingCanceled = false;
  @Getter
  @Setter
  private volatile boolean isDataSendingCompleted = false;
  @NonNull
  final private AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel;
  final private Map<String, Integer> instrumentLastPrice = new ConcurrentHashMap<>();

  @Override
  public <T> T receive(Class<T> type) {
    try {
      // Consumer will stop reading data once CANCEL or COMPLETE message is send.
      while (!isDataSendingCanceled && (!isDataSendingCompleted || !isAllPriceDataMessageConsumed())) {
        PriceDataMessage priceDataMessage = priceDataMessageChannel.get(batchId).poll();
        if (priceDataMessage != null) {
          priceDataMessage.getPriceDataList().stream().forEach(priceData -> {
            instrumentLastPrice.put(priceData.getId(), priceData.getPrice());
          });
          System.out.println("Received Message: "+ priceDataMessage);
          Thread.sleep(2000);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return type.cast(instrumentLastPrice);
  }

  private boolean isAllPriceDataMessageConsumed() {
    return priceDataMessageChannel.get(batchId).size() > 0 ? Boolean.FALSE : Boolean.TRUE;
  }

  @Override
  public Map<String, Integer> getLastPriceData() {
    while(true) {
      if(isDataSendingCompleted && isAllPriceDataMessageConsumed()) {
        return Map.copyOf(instrumentLastPrice);
      }
    }
  }

  @Override
  public void reset() {
    isDataSendingCompleted = false;
    isDataSendingCanceled = false;
    instrumentLastPrice.clear();
  }
}
