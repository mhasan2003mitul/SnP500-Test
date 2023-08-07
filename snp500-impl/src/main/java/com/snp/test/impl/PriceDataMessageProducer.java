package com.snp.test.impl;

import com.snp.test.api.PriceData;
import com.snp.test.api.PriceDataMessage;
import com.snp.test.api.SendMessageProvider;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(staticName = "of")
@Setter
@Getter
class PriceDataMessageProducer implements SendMessageProvider {

  @NonNull
  private int batchId;
  @NonNull
  private int chunkSize;
  private volatile boolean isStopped = Boolean.FALSE;
  @NonNull
  private List<PriceData> priceDataList;
  @NonNull
  private AbstractMap<Integer, BlockingQueue<PriceDataMessage>> priceDataMessageChannel;

  @Override
  public boolean send() {
    priceDataMessageChannel.putIfAbsent(batchId, new LinkedBlockingQueue<>());
    PriceDataMessage batchMessage;
    try {
      if(priceDataList.size() > chunkSize) {
        for(int i=0; i < priceDataList.size() && !isStopped; i = i+chunkSize) {
          List<PriceData> priceData;
          if(i + chunkSize < priceDataList.size()) {
             priceData = priceDataList.subList(i, i + chunkSize);
          } else {
            priceData = priceDataList.subList(i, priceDataList.size());
          }
          System.out.println("Send: "+ priceData);
          batchMessage = new PriceDataMessage(batchId, (i / chunkSize) + 1, priceData.size(), priceData);
          priceDataMessageChannel.get(batchId).put(batchMessage);
          Thread.sleep(1000);
        }
      } else {
        batchMessage = new PriceDataMessage(batchId, 1, priceDataList.size(), new ArrayList<>(priceDataList));
        priceDataMessageChannel.get(batchId).put(batchMessage);
        Thread.sleep(1000);
      }
      return Boolean.TRUE;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Boolean.FALSE;
  }
}
