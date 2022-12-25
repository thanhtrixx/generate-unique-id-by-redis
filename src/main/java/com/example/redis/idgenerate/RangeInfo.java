package com.example.redis.idgenerate;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class RangeInfo {

  private final long serverTime;

  private final long max;
}
