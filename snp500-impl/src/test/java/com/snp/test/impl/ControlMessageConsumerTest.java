package com.snp.test.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.snp.test.api.ControlCommandType;
import com.snp.test.api.ControlMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

class ControlMessageConsumerTest {

  @Test
  void receive() {
    BlockingQueue<ControlMessage> controlMessages = new LinkedBlockingQueue<>();

    // Send cancel message to the control message channel.
    CancelMessageProducer cancelMessageProducer = CancelMessageProducer.of(1,controlMessages);
    cancelMessageProducer.send();

    // Receive message from the control message channel.
    ControlMessageConsumer controlMessageConsumer = ControlMessageConsumer.of(controlMessages);
    ControlMessage controlMessage = controlMessageConsumer.receive(ControlMessage.class);

    assertEquals(TestDataUtil.BATCH_ID, controlMessage.getBatchId());
    assertEquals(ControlCommandType.CANCEL_BATCH_COMMAND,controlMessage.getCommandType());
  }
}