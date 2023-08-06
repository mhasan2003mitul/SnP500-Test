package com.snp.test.api;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ControlMessage {
  private ControlCommandType commandType;
  private int batchId;
  private int numberOfMessage;

  public static ControlMessage getStartBatchCommand(int batchId,int numberOfMessage) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.START_BATCH_COMMAND;
    message.batchId = batchId;
    message.numberOfMessage = numberOfMessage;
    return message;
  }

  public static ControlMessage getCancelBatchCommand(int batchId) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.CANCEL_BATCH_COMMAND;
    message.batchId = batchId;
    return message;
  }

  public static ControlMessage getCompleteBatchCommand(int batchId, int numberOfMessage) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.COMPLETE_BATCH_COMMAND;
    message.batchId = batchId;
    message.numberOfMessage = numberOfMessage;
    return message;
  }
}
