package com.snp.test.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class PriceDataMessage {
  private int batchId;
  private int chunkId;
  private int chunkSize;
  private List<PriceData> priceDataList;
}
