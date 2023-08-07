package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.SendMessageProvider;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
class StartMessageProducer implements SendMessageProvider {
  private int batchId;
  private int numberOfPriceData;
  private BlockingQueue<ControlMessage> controlMessageChannel;

  @Override
  public boolean send() {
    try {
      this.controlMessageChannel.put(ControlMessage.getStartBatchCommand(batchId, numberOfPriceData));
      return Boolean.TRUE;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Boolean.FALSE;
  }
}
