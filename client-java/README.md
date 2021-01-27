# Palisade Client (Java)

The Java Palisade Client API provides universal resource access a Palisade service. The design of the API loosely follows that of other well known API's (e.g. JDBC). This design decision was made to provide a familiar feel to accessing Palisade.

Of course there are differences. One of the big differences is that Palisade clients deal with returning resource files and not data in Columnar format.

### Example Usage

A unit test to assert that resources are returned may look like the following. Note that here we are using Micronaut to create endpoints for the following with Palisade:

* Palisade Service
* Filtered Resource Service
* Data Service

Micronaut will find these services and create and then inject an EmbeddedServer to expose them. The embedded server can be used to get the port which it is listening on. The port will be dynamically created enabling parallel tests.

```java
@MicronautTest
class FullTest {

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void testWithDownloadOutsideStream() throws Exception {

        var port = ""+embeddedServer.getPort();
(1)     var properties = Map.<String, String>of(
            "service.palisade.port", port,
            "service.filteredResource.port", port);

(2)     var session = ClientManager.openSession("pal://mrblobby@localhost/cluster", properties);
(3)     var query = session.createQuery(QueryInfoImpl.create(b -> b.resourceId("resource_id")));
(4)     var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

(5)     var resources = Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType() == MessageType.RESOURCE)
            .map(Resource.class::cast)
            .collect(Collectors.toList())
            .blockingGet();

        assertThat(resources).hasSizeGreaterThan(0);

(6)     var resource = resources.get(0);
        assertThat(resource.getLeafResourceId()).isEqualTo("resources/pi0.txt");

(7)     var download = session.fetch(resource);
        assertThat(download).isNotNull();

        var actual = download.getInputStream();
        var expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/pi0.txt");

(8)     assertThat(actual).hasSameContentAs(expected);


    }
}

```

1. Creates a map of properties to be passed to the client. Here we are overriding the port for the palisade and filtered resource service.
2. Uses the `ClientManager` to create a `Session` from a palisade url. Here we are passing the user via the authority section of the URI. If a user is also passed via a property, this user in the URI takes precedence.
3. A new Query is created by passing `QueryInfo`.
4. The query is executed. The request is submitted to Palisade at this point and a `CompleteableFuture` is returned asynchronously. Once Palisade has processed the request, the future will emit a `Publisher` of `Messages` instances.
5. Convert the `java.util.current.Flow.Publisher` to an RxJava `Flowable` in order to apply filtering and retrieval into a collection of `Resource` instances.
6. Use the first resource as a test and make sure it's not null
7. Using the session we fetch the reource. A `Download` instance is returned. At this point the request has been sent and received from the data service. The download opbject provides access to an `InputStream`. The data is not returned from the server until the input stream is first accessed.
8. Using AssertJ the two input streams are checked for equality.

### Client properties

__Note:__ These  properties will override any query parameters or other values within the url (e.g. port/user).

| Property | Description |
| --- | --- |
| service.user | The userId |
| service.password | Optional passord if required |
| service.palisade.port | The palisade service port. If not set will equal any port provided within the service.url |
| service.filteredResource.port | The port for the filtered resource (websocket) service |
| service.url | The main cluster url .e.g. pal://user@localhost:12345/cluster |

### URL Query Parameters

These parameters can be added to the url in the normal way.

__Note:__ These  parameters will be overriden by propeties 

| Parameter | Description |
| --- | --- |
| port | Base port for the palisade cluster |
| psport | The port for the Palisade Service |
| wsport | The port for the filtered resource service. Specifiy this if it is different from the palisade service. If not supplied it will be set to that of the palisade service |



Once the job is submitted, control is returned to the application without blocking. At this point the result only contains access to a CompletableFuture, which once complete returns the final state of the job.

## Technologies Used

### Runtime

* [Immutables](https://immutables.github.io/) - Java annotation processors to generate simple, safe and consistent value objects.
* [Jackson]() - JSON for Java. Handles all the (de)serialisation of objects to/from the Palisade servers.
* [JSON Flattener](https://github.com/wnameless/json-flattener) - A Java utility is used to FLATTEN nested JSON objects and even more to UNFLATTEN it back.

### Test Only

* [Junit5](https://junit.org/junit5/) - Needs no introduction :)
* [AssertJ](https://assertj.github.io/doc/) - Excellent testing library
* [Logback](http://logback.qos.ch/) - Great logging library. Used for testing.
* [Awaitility](https://github.com/awaitility/awaitility) - Good for asynchronous testing
* [Micronaut HTTP Server](https://micronaut.io/) - Used for testing
