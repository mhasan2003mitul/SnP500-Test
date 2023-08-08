package com.snp.test.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Data model of price data.
 */
@AllArgsConstructor
@Getter
@ToString
public class PriceData {
  private String id;
  private long asOf;
  private int price;
}
