package com.example.redis.idgenerate;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@AllArgsConstructor
public class RedisRangeProducer implements RangeProducer {

  private final RedisScript<List> script;

  private final RedisTemplate<String, String> redisTemplate;


  @Override
  public RangeInfo reserveRange(@NonNull String prefixKey, int reserveCount) {
    RangeInfo result = null;
    try {
      val redisResult = this.redisTemplate.execute(script,
        List.of(prefixKey, String.valueOf(reserveCount)));

      if (redisResult == null || redisResult.size() != 2) {
        throw new RuntimeException("Result from Redis invalid: " + redisResult);
      }

      result = RangeInfo.builder()
        .serverTime((long) redisResult.get(0))
        .max((long) redisResult.get(1))
        .build();
      return result;
    } finally {
      log.debug("Redis reserveRange: prefixKey={}, reserveCount={}, result={}", prefixKey,
        reserveCount, result);
    }
  }
}
