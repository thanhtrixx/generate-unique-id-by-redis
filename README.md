# Generate Unique ID by Redis 🚀

## Introduction

This project provides an efficient and scalable **unique ID generation system** leveraging Redis. Unlike traditional UUID-based approaches, this ID format is designed to optimize **database partitioning**, improving query performance and data distribution.

## Why Not Use UUID?

Many systems rely on UUIDs (e.g., `UUID.randomUUID()`) for generating unique IDs, but UUIDs have several downsides:

- **Poor Indexing Performance**: UUIDs are randomly distributed, making database indexing and partitioning inefficient.
- **Larger Storage Overhead**: UUIDs take up 128 bits, whereas numeric IDs can be much more compact.
- **No Order Guarantee**: UUIDs do not provide a sequential order, leading to performance issues in databases optimized for sequential writes.

This project provides a **partition-friendly** ID format that ensures uniqueness while improving database performance.

---

## Key Features ✨

### 1. **Optimized for Database Partitioning**
- **ID Format**: `[Prefix][Timestamp][Sequence]` (e.g., `ORDER_231025_000000123`)
  - **Prefix**: Categorizes data (e.g., ORDER, USER).
  - **Timestamp**: Date in `yyMMdd` format, enabling time-based partitioning.
  - **Sequence**: Incremental number, ensuring uniqueness.
- **Benefits**:
  - Simplifies time-based partitioning in databases.
  - Improves query performance and storage efficiency.
  - Avoids fragmentation issues common with UUIDs.

### 2. **Semi-Centralized Architecture**
- **Local Service**:
  - Generates IDs locally from a pre-reserved batch.
  - Reduces Redis dependency, improving performance.
- **Redis**:
  - Tracks the **highest reserved ID** to ensure global uniqueness.
- **Benefits**:
  - Minimizes latency by reducing Redis calls.
  - Ensures consistency across distributed systems.

### 3. **Race Condition Avoidance**
- **Producer-Consumer Model**:
  - **Producer**: Reserves a batch of IDs from Redis and generates IDs into a `LinkedBlockingQueue`.
  - **Consumer**: Safely retrieves IDs from the queue for use.
- **Benefits**:
  - Prevents race conditions in multi-threaded environments.
  - Ensures thread-safe ID generation and distribution.

---

## How It Works 🛠️

1. **ID Preallocation**:
   - The local service requests a range of IDs from Redis (e.g., `1000 - 1999`).
   - Redis updates the `max ID` and returns the reserved range.

2. **Local ID Generation**:
   - The service generates IDs in the format `[Prefix][Timestamp][Sequence]`.
   - IDs are stored in a `LinkedBlockingQueue` for thread-safe access.

3. **Concurrency Handling**:
   - Multiple threads can safely retrieve IDs from the queue without conflicts.
   - When the queue is nearly empty, the producer reserves a new batch from Redis.

4. **Redis Updates**:
   - Redis ensures global uniqueness by tracking the highest reserved ID.

---

## Components 🧩

- **`IdGenerator`**: Generates IDs locally based on the reserved range.
- **`RangeProducer`**: Fetches new ID ranges from Redis.
- **`RedisRangeProducer`**: Implements Redis-based ID reservation using a Lua script.
- **`RedisConfig`**: Configures Redis connectivity and Lua script integration.
- **`RedisApplication`**: Main application for multi-threaded ID generation and testing.

---

## Setup & Usage 📦

### Prerequisites
- Java 11+
- Redis Server
- Spring Boot 2.7.7

### Configuration
Add the following properties to `application.properties`:
```properties
keyPrefix=ORDER       # Prefix for IDs (e.g., ORDER, USER)
reserveCount=100      # Number of IDs to reserve per batch
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

### Comparison with UUID

| Feature              | Redis-based ID       | UUID                  |
|-----------------------|----------------------|-----------------------|
| Partitioning          | Optimized (time-based) | Poor (random)         |
| Performance           | High (batch reserve)  | Low (random)          |
| Storage Size          | Compact (20-30 chars) | Large (36 chars)      |
| Sequential Order      | Yes (incremental)     | No                    |
| Complexity            | Moderate              | Simple                |

### Benefits
- **Scalability**: Each service instance can generate IDs independently.
- **Efficiency**: Reduces Redis calls, improving performance.
- **Data Partitioning**: Structured ID format helps databases distribute and index data more efficiently.

---

## Conclusion

This ID generation approach provides the **best of both worlds**:

- **Scalability**: Each service instance can generate IDs independently.
- **Efficiency**: Reduces Redis calls, improving performance.
- **Data Partitioning**: Unlike UUIDs, the structured ID format helps databases distribute and index data more efficiently.

This makes it an excellent choice for **high-performance distributed systems**!

---

## Contributing 🤝

Contributions are welcome! Please:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes.
4. Push to the branch.
5. Open a Pull Request.

---

## License 📄

MIT License (See [LICENSE](LICENSE) for details)
