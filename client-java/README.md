
<!---
Copyright 2018-2021 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

# Palisade Client (Java)

The Java Palisade Client API provides universal resource access a Palisade service. The design of the API loosely follows that of other well known API's (e.g. JDBC). This design decision was made to provide a familiar feel to accessing Palisade.

Of course there are differences. One of the big differences is that Palisade clients deal with returning resource files and not data in Columnar format.

### Example Usage

A unit test to assert that resources are returned may look like the following. Note that here we are using Micronaut to create end-points for the following with Palisade:

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

1. Creates a map of properties to be passed to the client. Here we are overriding the port for the Palisade Service and Filtered Resource Service.
2. Uses the `ClientManager` to create a `Session` from a palisade URL. Here we are passing the user via the authority section of the URI. If a user is also passed via a property, this user in the URI takes precedence.
3. A new Query is created by passing `QueryInfo`.
4. The query is executed. The request is submitted to Palisade at this point and a `CompleteableFuture` is returned asynchronously. Once Palisade has processed the request, the future will emit a `Publisher` of `Messages` instances.
5. Convert the `java.util.current.Flow.Publisher` to an RxJava `Flowable` in order to apply filtering and retrieval into a collection of `Resource` instances.
6. Use the first resource as a test and make sure it's not null
7. Using the session we fetch the resource. A `Download` instance is returned. At this point the request has been sent and received from the data service. The download object provides access to an `InputStream`. The data is not returned from the server until the input stream is first accessed.
8. Using AssertJ the two input streams are checked for equality.

### Client properties

__Note:__ These  properties will override any query parameters or other values within the URL (e.g. port/user).

| Property | Description |
| --- | --- |
| service.user | The userId |
| service.password | Optional password if required |
| service.palisade.port | The Palisade Service port. If not set will equal any port provided within the service.url |
| service.filteredResource.port | The port for the Filtered Fesource (websocket) Fervice |
| service.url | The main cluster URL .e.g. pal://user@localhost:12345/cluster |

### URL Query Parameters

These parameters can be added to the URL in the normal way.

__Note:__ These  parameters will be overridden by properties 

| Parameter | Description |
| --- | --- |
| port | Base port for the Palisade cluster |
| psport | The port for the Palisade Service |
| wsport | The port for the Filtered Resource Rervice. Specify this if it is different from the Palisade Service. If not supplied it will be set to that of the Palisade Service |

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
