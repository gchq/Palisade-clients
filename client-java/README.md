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