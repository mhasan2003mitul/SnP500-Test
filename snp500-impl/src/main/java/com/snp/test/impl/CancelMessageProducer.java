package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.SendMessageProvider;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
class CancelMessageProducer implements SendMessageProvider {
  private int batchId;
  private BlockingQueue<ControlMessage> controlMessageChannel;

  @Override
  public boolean send() {
    try {
      this.controlMessageChannel.put(ControlMessage.getCancelBatchCommand(batchId));
      return Boolean.TRUE;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Boolean.FALSE;
  }
}
