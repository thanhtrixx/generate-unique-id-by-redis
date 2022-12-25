package com.example.redis.idgenerate;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class IdGenerator {

  private static final int WAITING_MILLIS = 200;
  private static final int PADDING_CHARACTER_LENGTH = 9;

  private final RangeProducer rangeProducer;
  private final LinkedBlockingQueue<String> generatedIds;
  private final String keyPrefix;
  private final int reserveCount;

  private final AtomicBoolean isRunning = new AtomicBoolean(false);
  private final AtomicBoolean isReady = new AtomicBoolean(false);

  private final Executor rangeProducerExecutor;

  public IdGenerator(RangeProducer rangeProducer,
    @Value("${keyPrefix:outgoing}") String keyPrefix,
    @Value("${reserveCount:100}") int reserveCount,
    @Value("${numberProducer:1}") int numberProducer
  ) {
    this.rangeProducer = rangeProducer;
    this.keyPrefix = keyPrefix;
    this.reserveCount = reserveCount;
    this.generatedIds = new LinkedBlockingQueue<>(reserveCount);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown IdGenerator...");
      isRunning.set(false);
      isReady.set(false);
    }));

    rangeProducerExecutor = Executors.newFixedThreadPool(numberProducer,
      new ThreadFactoryBuilder().setDaemon(true).build());

    for (int i = 0; i < numberProducer; i++) {
      rangeProducerExecutor.execute(this::generateId);
    }
  }

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd")
    .withLocale(Locale.getDefault())
    .withZone(ZoneOffset.UTC);

  @SneakyThrows
  private void generateId() {
    isRunning.set(true);
    StringBuilder sb = new StringBuilder();
    var retryCount = 0;
    while (isRunning.get()) {
      val rangeInfo = rangeProducer.reserveRange(keyPrefix, reserveCount);

      if (rangeInfo == null) {
        try {
          Thread.sleep(WAITING_MILLIS);
        } catch (InterruptedException e) {
          log.warn("Sleeping error", e);
        }

        retryCount++;
        if (retryCount > 3) {
          log.error("Stop generate");
          isRunning.set(false);
        }

        continue;
      }
      isReady.set(true);
      String prefix = formatter.format(Instant.ofEpochSecond(rangeInfo.getServerTime()));

      for (long i = rangeInfo.getMax() - reserveCount + 1; i < rangeInfo.getMax() + 1; i++) {
        val idString = String.valueOf(i);
        sb.setLength(0);
        sb
          .append(prefix)
          //left padding
          .append(
            idString.length() >= PADDING_CHARACTER_LENGTH
              ? ""
              : "0".repeat(PADDING_CHARACTER_LENGTH - idString.length())
          )
          .append(idString);

        generatedIds.put(sb.toString());
      }
    }
  }

  @SneakyThrows
  public String getId() {
    while (!isReady.get()) {
      Thread.sleep(WAITING_MILLIS);
    }

    return generatedIds.poll(1, TimeUnit.SECONDS);
  }
}
