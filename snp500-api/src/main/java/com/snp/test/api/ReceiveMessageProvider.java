package com.snp.test.api;

/**
 * Define contract for consumer to consume data from shared QUEUE.
 */
public interface ReceiveMessageProvider {

  <T> T receive(Class<T> type);
}
