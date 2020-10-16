# Palisade Client (Java)

The palisade client provides access to resources.

Example usage:

```
var client = (JavaClient) Client
    .create(Map.of(
        "palisade.client.url", "http://localhost:8081",
        "palisade.download.threads", "2",
        "palisade.download.path", "/tmp"));

var futureResult = client
    .submit(b -> b
        .userId("user_id")
        .resourceId("resource_id")
        .purpose("purpose")
        .context(Map.of(
             "key1", "value1",
             "key2", "value2"))
        .receiverSupplier(() -> new FileReceiver());
```

Once the job is submitted control is returned to the application. The result contains nothing at this moment. The plan for the future is for the result to contain details of the submitted job. This would be the configuration and a `Future` that can be used to block until the job is complete.

Note that the configuration will probably change once more eyes are on the project.

Also note this is the first draft of the new client.

## Overview

## Logical Diagram

![Logical Diagram](doc/java-client-logical-diagram-1.svg){width=800}

## Code

The main classes within the client are as follows:

* JavaClient - This is the main entry point and the manager of all events
* ClientConfig - This is the class that the configuration is loaded into
* DownloadManager - Manages the submission of requests, contains the thread pool
* Downloader - Executes requests to the data service and provides a stream to a Receiver
* FileReceiver - Reads a data stream and writes to a file
* ResourceClient - Manages the flow of message to/from the Filtered resource Service via web sockets
* DIFactory - Contains the wiring for the DI container
* ByteBufferInputStream - Takes an RxJava Flowable<java.nio.ByteBuffer> and exposes it as a single InputStream

## Technologies Used

### Runtime

* [Micronaut](https://micronaut.io/) - Used for its compile time, fully type safe DI, Event bus, HTTP Client and HTTP server (for testing only)
* [Tyrus](https://tyrus-project.github.io/) - Open source [JSR 356 - Java API for WebSocket](http://java.net/projects/websocket-spec) reference implementation
* [Immutables](https://immutables.github.io/) - Java annotation processors to generate simple, safe and consistent value objects.
* [Jackson]() - JSON for Java. Handles all the (de)serialisation of objects to/from the Palisade servers.

### Test Only

* [Junit5](https://junit.org/junit5/) - Needs no introduction :)
* [AssertJ](https://assertj.github.io/doc/) - Excellent testing library
* [Logback](http://logback.qos.ch/) - Great logging library. Used for testing.
* [Awaitility](https://github.com/awaitility/awaitility) - Good for asynchronous testing
* [Reflections](https://github.com/ronmamo/reflections) - Java runtime metadata analysis. Used here for testing.
* [Micronaut HTTP Server](https://micronaut.io/) - Used for testing
