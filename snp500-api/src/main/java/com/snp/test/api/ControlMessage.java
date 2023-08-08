package com.snp.test.api;

import lombok.Getter;
import lombok.ToString;

/**
 * Control message definition for created control message and send over shared control message queue.
 */
@Getter
@ToString
public class ControlMessage {
  private ControlCommandType commandType;
  private int batchId;
  private int numberOfMessage;
  private String instrumentId;

  /**
   * @param batchId batch id for a batch upload
   * @param numberOfMessage number of messages in a batch
   * @return A start batch control command message which will send to the shared control message queue to notify a batch upload will start.
   */
  public static ControlMessage getStartBatchCommand(int batchId,int numberOfMessage) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.START_BATCH_COMMAND;
    message.batchId = batchId;
    message.numberOfMessage = numberOfMessage;
    return message;
  }

  /**
   * @param batchId batch id for canceling a batch upload
   * @return A cancel batch control command message which will send to the shared control message queue to notify a batch upload has been canceled.
   */
  public static ControlMessage getCancelBatchCommand(int batchId) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.CANCEL_BATCH_COMMAND;
    message.batchId = batchId;
    return message;
  }

  /**
   * @param batchId batch id for a batch upload
   * @param numberOfMessage number of messages in a batch
   * @return A complete batch control command message which will send to the shared control message queue to notify a batch upload has been completed.
   */
  public static ControlMessage getCompleteBatchCommand(int batchId, int numberOfMessage) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.COMPLETE_BATCH_COMMAND;
    message.batchId = batchId;
    message.numberOfMessage = numberOfMessage;
    return message;
  }

  public static ControlMessage getRequestInstrumentPriceDataCommand(String instrumentId, int batchId) {
    ControlMessage message = new ControlMessage();
    message.commandType = ControlCommandType.REQUEST_INSTRUMENT_PRICE_DATA_COMMAND;
    message.instrumentId = instrumentId;
    message.batchId = batchId;
    return message;
  }
}
