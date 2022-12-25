package com.example.redis;

import com.example.redis.idgenerate.IdGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication
@AllArgsConstructor
public class RedisApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(RedisApplication.class, args);
  }

  private IdGenerator idGenerator;

  private final AtomicLong counter = new AtomicLong();


  @Override
  public void run(String... args) throws Exception {
    val numberThreads = Integer.parseInt(args[0]);
    val idPerThread = Integer.parseInt(args[1]);
    val executors = Executors.newFixedThreadPool(numberThreads,
      new ThreadFactoryBuilder().setDaemon(true).build());

    val countDownLatch = new CountDownLatch(numberThreads);
    try (val writer = new BufferedWriter(new FileWriter("ids", true))) {
      for (int i = 0; i < numberThreads; i++) {
        executors.execute(getTask(idPerThread, countDownLatch, writer));
      }

      countDownLatch.await();
    }
  }

  private Runnable getTask(int idPerThread, CountDownLatch countDownLatch, BufferedWriter writer) {
    return () -> {
      log.info("Executing");
      val ids = new HashSet<String>();
      for (int i = 0; i < idPerThread; i++) {
        val id = idGenerator.getId();
        if (id == null) {
          log.warn("id = null");
        }
        ids.add(id);
        counter.incrementAndGet();
      }
      log.info("Done with set count {} & counter {}", ids.size(), counter.get());
      writeIds(ids, writer);
      countDownLatch.countDown();
    };
  }

  @SneakyThrows
  private synchronized void writeIds(Set<String> ids, BufferedWriter writer) {
    for (String id : ids) {
      writer.write(id);
      writer.newLine();
    }
    writer.flush();
  }
}
