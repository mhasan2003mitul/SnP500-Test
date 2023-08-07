package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.SendMessageProvider;
import java.util.concurrent.BlockingQueue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
class CompleteMessageProducer implements SendMessageProvider {
  @NonNull
  private int batchId;
  @NonNull
  private int numberOfPriceData;
  @NonNull
  private BlockingQueue<ControlMessage> controlMessageChannel;

  @Override
  public boolean send() {
    try {
      this.controlMessageChannel.put(ControlMessage.getCompleteBatchCommand(batchId, numberOfPriceData));
      return Boolean.TRUE;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Boolean.FALSE;
  }
}
