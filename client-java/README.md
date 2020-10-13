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
        .classname("classname")
        .purpose("purpose")
        .requestId("request_id")
        .resourceId("resource_id")
        .userId("user_id")
        .receiverSupplier(() -> new FileReceiver());
```

Once the job is submitted control is returned to the application. The result contains nothing at this moment. The plan for the future is for the result to contain details of the submitted job. This would be the configuration and a `Future` that can be used to block until the job is complete.

Note that the configuration will probably change once more eyes are on the project.

Also note this is the first draft of the new client.

## Overview

The main classes within the client are as follows:

* JavaClient - This is the main entry point and the manager of all events
* ClientConfig - This is the class that the configuration is loaded into
* DownloadManager - Manages the submission of requests, contains the thread pool
* Downloader - Executes requests to the data service and provides a stream to a Receiver
* FileReceiver - Reads a data stream and writes to a file
* ResourceClient - Manages the flow of message to/from the Filtered resource Service via websockets
* DIFactory - Contains the wiring for the DI container
* ByteBufferInputStream - Takes a Flowable<jana.nio.ByteBuffer> and exposes them as a single InputStream