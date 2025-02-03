# Generate Unique ID by Redis 🚀

## Introduction

This project provides an efficient and scalable **unique ID generation system** using Redis. Unlike traditional UUID-based approaches, this method optimizes **database partitioning**, improves query performance, and ensures structured ID generation.

## Why Not Use UUID? 🤔

UUIDs (`UUID.randomUUID()`) are common for unique ID generation, but they have significant drawbacks:

- 🚫 **Poor Indexing** – UUIDs are random, making database indexing inefficient.
- 📏 **Larger Storage** – UUIDs require 128 bits, whereas numeric IDs are more compact.
- ❌ **No Sequential Order** – UUIDs do not provide a time-based order, reducing performance in sequential-write databases.

This project offers a **partition-friendly** ID format that ensures uniqueness while optimizing database performance.

---

## Key Features ✨

### 🔹 Optimized for Database Partitioning

- **ID Format:** `[Prefix][Timestamp][Sequence]` (e.g., `ORDER_231025_000000123`)
  - **Prefix**: Categorizes data (e.g., `ORDER`, `USER`).
  - **Timestamp**: Date in `yyMMdd` format for time-based partitioning.
  - **Sequence**: Incremental number ensuring uniqueness.
- **Benefits:**
  - Efficient time-based partitioning in databases.
  - Faster queries and optimized storage.
  - Avoids fragmentation issues common with UUIDs.

### 🔹 Semi-Centralized Architecture

- **Local Service**: Generates IDs from a pre-reserved batch, reducing Redis dependency.
- **Redis Backend**: Tracks the highest reserved ID for global uniqueness.
- **Benefits:**
  - Minimizes latency by reducing Redis calls.
  - Ensures consistency across distributed systems.

### 🔹 Race Condition Prevention

- **Producer-Consumer Model**:
  - **Producer**: Reserves a batch of IDs from Redis and pre-generates IDs.
  - **Consumer**: Retrieves IDs from a thread-safe queue.
- **Benefits:**
  - Prevents race conditions in multi-threaded environments.
  - Ensures smooth ID generation without conflicts.

---

## How It Works 🛠️

1. **ID Preallocation**

   - The local service requests a range of IDs from Redis (e.g., `1000 - 1999`).
   - Redis updates the highest reserved ID and returns the range.

2. **Local ID Generation**

   - The service generates IDs using the format `[Prefix][Timestamp][Sequence]`.
   - A `LinkedBlockingQueue` stores IDs for fast retrieval.

3. **Concurrency Handling**

   - Multiple threads retrieve IDs safely from the queue.
   - When the queue is almost empty, the producer requests a new batch from Redis.

4. **Redis Ensures Global Uniqueness**

   - Redis maintains the highest allocated ID, preventing duplication.

---

## Components 🧩

- **`IdGenerator`** – Generates IDs based on reserved ranges.
- **`RangeProducer`** – Requests new ID ranges from Redis.
- **`RedisRangeProducer`** – Implements Redis-based ID reservation via Lua script.
- **`RedisConfig`** – Configures Redis connection and script integration.
- **`RedisApplication`** – Main application for multi-threaded ID generation.

---

## Setup & Usage 📦

### Prerequisites

- Java 11+
- Redis Server
- Spring Boot 2.7.7

### Configuration (`application.properties`)

```properties
keyPrefix=ORDER       # Prefix for IDs (e.g., ORDER, USER)
reserveCount=100      # Number of IDs reserved per batch
numberProducer=2      # Number of producer threads
```

### Running the Application

1. Start Redis.
2. Run the application:
   ```sh
   ./gradlew bootRun
   ```

---

## Why This Approach? 🌟

### ✅ UUID vs. Redis-Based ID Comparison

| Feature              | Redis-based ID           | UUID               |
| -------------------- | ------------------------ | ------------------ |
| **Partitioning**     | ✅ Optimized (time-based) | ❌ Poor (random)    |
| **Performance**      | ✅ High (batch reserve)   | ❌ Low (random)     |
| **Storage Size**     | ✅ Compact (20-30 chars)  | ❌ Large (36 chars) |
| **Sequential Order** | ✅ Yes (incremental)      | ❌ No               |
| **Complexity**       | ⚖️ Moderate              | ✅ Simple           |

### 🚀 Benefits

- **Scalability** – Each service instance generates IDs independently.
- **Efficiency** – Reduces Redis calls for better performance.
- **Better Partitioning** – Unlike UUIDs, structured IDs help database indexing.

---

## Contributing 🤝

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a new branch:
   ```sh
   git checkout -b feature/your-feature
   ```
3. Make your changes and commit them.
4. Push to the branch.
5. Open a Pull Request.

---

## License 📄

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
