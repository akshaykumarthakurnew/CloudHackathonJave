Quarkus Kafka/Panache Quickstart
================================

This project illustrates how you can interact with Apache Kafka using MicroProfile Reactive Messaging and Hibernate with Panache.
The project uses:

* RESTEasy Reactive
* Reactive Messaging and its connector for Kafka
* (Classic) Hibernate with Panache

_NOTE:_ The [kafka-panache-reactive-quickstart](../kafka-panache-reactive-quickstart) provides the same example but using Hibernate Reactive.

## Start the application

The application can be started using: 

```bash
mvn quarkus:dev
```

_NOTE:_ The database and Kafka broker are started using Dev Services

Then, open your browser to `http://localhost:8080/prices`, and you should get the set of prices written in the database.
Every 5 seconds, a new price is generated, sent to a Kafka topic, received by a Kafka consumer and written to the database.
Refresh the page to see more prices.

## Anatomy

* `PriceGenerator` - a bean generating random price. They are sent to a Kafka topic.
* `PriceStorage` - on the consuming side, the `PriceStorage` receives the Kafka message and write it into the database using Hibernate with Panache
* `PriceResource`  - the `PriceResource` retrieves the prices from the database and send them into the HTTP response

## Running in native

You can compile the application into a native binary using:

`mvn clean install -Pnative`

As you are not in dev or test mode, you need to start a PostgreSQL instance and a Kafka broker.
To start them, just run `docker-compose up -d`.

Then, run the application with:

`./target/kafka-panache-quickstart-1.0.0-SNAPSHOT-runner` 