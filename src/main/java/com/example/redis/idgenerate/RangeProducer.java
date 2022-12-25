package com.example.redis.idgenerate;

import lombok.NonNull;

public interface RangeProducer {

  RangeInfo reserveRange(@NonNull String prefix, int reserveCount);
}
