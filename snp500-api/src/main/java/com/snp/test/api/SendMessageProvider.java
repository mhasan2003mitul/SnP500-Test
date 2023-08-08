package com.snp.test.api;

/**
 * Define contract for producer to produce data to shared QUEUE.
 */
public interface SendMessageProvider {
  public boolean send();
}
