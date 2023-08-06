package com.snp.test.impl;

import com.snp.test.api.ControlMessage;
import com.snp.test.api.ReceiveMessageProvider;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
class ControlMessageConsumer implements ReceiveMessageProvider {

  private BlockingQueue<ControlMessage> controlMessageChannel;

  @Override
  public <T> T receive(Class<T> type) {
    ControlMessage controlMessage = null;
    try {
      controlMessage = controlMessageChannel.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return type.cast(controlMessage);
  }
}
