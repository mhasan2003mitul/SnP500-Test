package com.snp.test.api;

public interface ReceiveMessageProvider {
  public <T> T receive(Class<T> type);
}
