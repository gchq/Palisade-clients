# Palisade Client (Java)

The palisade client provides access to resources.

Example usage:

```
client = Client.create(Map.of(
    Configuration.KEY_SERVICE_HOST, "host.my",
    Configuration.KEY_SERVICE_PORT, 12091,
    Configuration.KEY_DOWNLOAD_THREADS, 2));

var futureResult = client
    .submit(b -> b
        .userId("user_id")
        .resourceId("resource_id")
        .purpose("purpose")
        .properties(Map.of(
             "key1", "value1",
             "key2", "value2"))
        .receiverClass("my.class.name");
        
var state = result.future().join();

for (IStateDownload dl : state.getDownloads()) {
    System.out.println(dl.getResourceId());
}
```

Once the job is submitted control is returned to the application without blocking. At this point the result only contains access to a CompletableFuture, which once complete returns the final state of the job.

## Overview

## Logical Diagram

![Logical Diagram](doc/java-client-logical-diagram-1.svg){width=800}

## Code

The main classes within the client are as follows:

* JavaClient - This is the main entry point and the manager of all events
* DownloadManager - Manages the submission of requests, contains the thread pool
* Downloader - Executes requests to the data service and provides a stream to a Receiver
* FileReceiver - Reads a data stream and writes to a file
* ResourceClient - Manages the flow of message to/from the Filtered resource Service via web sockets

## Technologies Used

### Runtime

* [Immutables](https://immutables.github.io/) - Java annotation processors to generate simple, safe and consistent value objects.
* [Jackson]() - JSON for Java. Handles all the (de)serialisation of objects to/from the Palisade servers.

### Test Only

* [Junit5](https://junit.org/junit5/) - Needs no introduction :)
* [AssertJ](https://assertj.github.io/doc/) - Excellent testing library
* [Logback](http://logback.qos.ch/) - Great logging library. Used for testing.
* [Awaitility](https://github.com/awaitility/awaitility) - Good for asynchronous testing
* [Reflections](https://github.com/ronmamo/reflections) - Java runtime metadata analysis. Used here for testing.
* [Micronaut HTTP Server](https://micronaut.io/) - Used for testing
