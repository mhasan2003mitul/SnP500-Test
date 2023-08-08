package com.snp.test.api;

/**
 * Define list of control command type for exchanging messages.
 */
public enum ControlCommandType {
  // Used for start a batch upload
  START_BATCH_COMMAND,
  // Used for stop a batch upload
  CANCEL_BATCH_COMMAND,
  // Used for signaling a bath upload is completed
  COMPLETE_BATCH_COMMAND,
  REQUEST_INSTRUMENT_PRICE_DATA_COMMAND
}
