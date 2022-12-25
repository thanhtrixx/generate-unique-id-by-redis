package com.example.redis;

import java.util.Collections;
import java.util.List;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {

  @Bean
  public RedisScript<List<Long>> script() {
    val scriptSource = new ClassPathResource("create-counter.lua");

    return RedisScript.of(scriptSource, (Class<List<Long>>) Collections.<String>emptyList().getClass());
  }
}
