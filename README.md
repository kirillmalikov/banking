# TUUM Test Assignment

## The Solution

After a brief research about banking applications, I came across multiple articles that referred to CQRS/ES
(Command and Query Responsibility Segregation and Event Sourcing) architectural style.
And I decided not to write just another CRUD solution, but to implement another approach,
though it is a very simplified version, however, it is just right to fit in the scope of this task in my opinion.
I made a diagram to abstractly represent which parts the system is composed of.
Also, a couple of links to the articles that I found useful: [here](https://memo.bank/en/magazine/choosing-an-architecture),
[here](https://martinfowler.com/bliki/CQRS.html) and [here](https://martinfowler.com/eaaDev/EventSourcing.html)

![diagram](architecture.svg)

## Used technologies

* Java 17
* SpringBoot
* MyBatis
* Postgres
* RabbitMQ
* JUnit
* Mockito
* Gradle

## Running the project

1. Download .zip or clone project from GitHub.

2. Use docker to run databases and MQ images
   ```sh
   docker compose up
   ```

3. Build the project using gradle
   ```sh
   ./gradlew build
   ```

4. Run the project
   ```sh
   ./gradlew bootRun
   ```

## How to use

### Endpoints

- Create a new account

| Endpoint (POST)           |
|---------------------------|
| localhost:8080/v1/account |  

Example request body:
   ```sh
   {
    "customerId": "8731eb7c-cc52-4fdd-9650-44d1cf9527ad",
    "country": "EE",
    "currencies": [
        "EUR", "USD", "GBP", "SEK"
    ]
}
   ```

- Get account by ID

| Endpoint (GET)                        |
|---------------------------------------|
| localhost:8080/v1/account/{accountId} |  

- Create a new transaction

| Endpoint (POST)                |
|--------------------------------|
| localhost:8080/v1/transaction  |  

Example request body:
   ```sh
  {
    "accountId": "e019f931-5b0c-48f6-87aa-1af3e803a3dc",
    "currency": "EUR",
    "amount": 10.99,
    "direction": "IN",
    "description": "description"
  }
   ```

- Get all transactions by account id

| Endpoint (GET)                            |
|-------------------------------------------|
| localhost:8080/v1/transaction/{accountId} |  


* RabbitMQ Management panel is located at http://localhost:15672/

* PostgreSQL:

    * Event Store:
  
    | ip       | localhost:5432 |
    |----------|----------------|
    | DB       | banking-es     |
    | user     | postgres       |
    | password | postgres       |

  * Projection database:

   | ip       | localhost:5433 |
   |----------|----------------|
   | DB       | banking        |
   | user     | postgres       |
   | password | postgres       |


## Load testing results

 * Used tool: *JMeter*
 * API: POST `/v1/account`.
 * This simulates also a transaction creation for different accounts (to some extent)

   | Thread count | Loop count | Error Rate | Throughput(sec) |
   |--------------|------------|------------|-----------------|
   | 1            | 100000     | 0.00%      | ~220            |
   | 10           | 100000     | 0.00%      | ~1300           |
   | 100          | 100000     | 0.00%      | ~1300           |
   | 100          | 1000000    | 0.00%      | ~1100           |
   | 1000         | 10         | 0.00%      | ~1200           |

Read operations give ~1500-2000 RPS

## Explanation of important choices in my solution

* The tricky part was to properly configure multiple data sources, especially of the same type (PostgreSQL).
  But in the end, the goal was achieved, so we have two data sources, two MyBatis mappers and session factories, separate flyway migration capabilities, and separate transaction managers.


* Due to the complexity of multiple data sources, was decided to make some compromises with the testing setup. So integration tests are made on 'production' databases without rollback.
  I am aware of this and this is just a corner cut in the scope of this particular task.
  Possible solutions would be to separate integration tests of different parts of the system, use test databases, test containers, etc. Simplified approach was used.
  In reality, this may be even separate microservices, so it wouldn't be a problem.


* Since I haven't used MyBatis before, was decided to try to implement everything using annotations, because XML seemed more complicated in such a short time.


* Aggregate class - here I tried to encapsulate the logic as reasonably as possible and not make it a God Class.
  Need mindset switch to deal with it after the DDD approach, though DDD is used in the 'projection' part of the system.


* Snapshots of aggregate are made each 5th version (event) in order to optimize recreation of account state. 


* Optimistic locking and event versioning was implemented. Though I'm not sure what is the 'industry standard' for such systems, need more case studies.
Current setup does not support high throughput for one account, but who makes thousands transactions per second to its own bank account :thinking: ?


* Custom exception handling was introduced to return meaningful information to the 'client'.


* Logging was completely ignored due to the lack of time. But it also seems out of the scope of this task.


## Horizontal scaling

* multiple instances and load balancers to evenly distribute traffic among these instances
* data sources - sharding, read-only replicas
* more sophisticated message producing/consuming logic to fully utilize async capabilities considering concurrency
* more efficient data sources, event stores, message brokers for its purposes
* completely decoupled system - write and read parts may be separate microservices. each part may be optimized separately and scaled asynchronously
* caching 
