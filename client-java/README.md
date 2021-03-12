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

The Java Palisade Client API provides universal resource access to a Palisade cluster.

### API Design

The design of the API loosely follows that of other well known API's. This design decision was made to provide a familiar feel to accessing Palisade.

Of course there are differences. One of the big differences is that Palisade clients deal with returning resource files and not data in Columnar format.

#### URL

The URL follows the JDBC specification of not exposing the underlying communication protocol. To this end the scheme is set as `pal:[subname]`.

If you are familiar with the JDBC url, then the Palisade URL should be familiar. See the examples below:

```
pal://eve@localhost/cluster?userid=alive
pal://localhost/cluster?userid=alive
pal://localhost:1234/cluster?userid=alice&wsport=4321
```

Note that any user passed as part of the authority portion of the URL (e.g. "eve" in the above example) will simply be copied to the create Palisade Service and Filtered Resource Service URIs. This use is not the `user_id` that is passed as part of the
REST call to the Palisade Service. The user id is passed via a property (`service.userid`) or as a query parameter (userid).

#### Interfaces

The API is split into many different interfaces, which again, are loosely based upon those of JDBC. Some of these interfaces include:

| Interface | Description |
| --- | --- |
| Client | This is analogous to JDBCs driver. This class provides access to actually open and retrieve a session to the Palisade cluster. Clients are not instantiated directly, but by the `ClientManager `asking the client whether it supports a given URL. This way the user of the API does not need to know about its implementation. |
| Session | This is roughly the same as JDBCs Connection class. A `Session` provides access to create queries and fetch downloads. At this point there is no security for a session as Palisade does not require it. If this changes in the future, the client API will be unaffected. |
| Query | This is the instance that sends the request to the Palisade Service. This is where the client deviates from JDBC as the design for this is (very) loosely based upon Hibernate's Query. |
| QueryResponse | Once the query is executes and the `Query` has returned this via a `Future`, a stream of `Message`'s can be retrieved. This class abstracts the underlying mechanisms of how the Filtered Resource Service is accessed. This has no analogue to JDBC or Hibernate as those libraries do not support streams yet. |
| Message | Two types of messages can be returned from the Filtered Resource Service and these are abstracted into two subclasses of `Message`. The design choice was to either have two sub types, or have a single type (resource) that can contain an Error. Either way is not wrong. This could change quite easily if needed. Currently two subclasses exist for Message. These are `Error` and `Resource`. |
| Download |  A `Download` is retrieved by passing a `Resource` object to the `Session`. The Download abstracts the call to the data service and provides access to an `InputStream` to consume its contents. |

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

        var port = "" + embeddedServer.getPort();
        (1) var properties = Map.<String, String>of(
            "service.userid", "alice",
            "service.palisade.port", port,
            "service.filteredResource.port", port);

        (2) var session = ClientManager.openSession("pal://eve@localhost/cluster", properties);
        (3) var query = session.createQuery("good_morning", Map.of("purpose", "Alice's purpose"));
        (4) var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

        (5) var resources = Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType() == MessageType.RESOURCE)
            .map(Resource.class::cast)
            .collect(Collectors.toList())
            .blockingGet();

        assertThat(resources).hasSizeGreaterThan(0);

        (6) var resource = resources.get(0);
        assertThat(resource.getLeafResourceId()).isEqualTo("resources/test-data-0.txt");

        (7) var download = session.fetch(resource);
        assertThat(download).isNotNull();

        var actual = download.getInputStream();
        var expected = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("resources/test-data-0.txt");

        (8) assertThat(actual).hasSameContentAs(expected);


    }
}

```

1. Creates a map of properties to be passed to the client. Here we are overriding the port for the Palisade Service and Filtered Resource Service.
2. Uses the `ClientManager` to create a `Session` from a Palisade URL. Here we are passing the user via the provided property map. If a user is also passed via a query parameter, this user in the query takes precedence.
3. A new Query is created by passing a query string and an optional map of properties.
4. The query is executed. The request is submitted to Palisade at this point and a `CompleteableFuture` is returned asynchronously. Once Palisade has processed the request, the future will emit a `Publisher` of `Messages` instances.
5. Convert the `java.util.current.Flow.Publisher` to an RxJava `Flowable` in order to apply filtering and retrieval into a collection of `Resource` instances.
6. Use the first resource as a test and make sure it's not null
7. Using the session we fetch the resource. A `Download` instance is returned. At this point the request has been sent and received from the Data Service. The download object provides access to an `InputStream`. The data is not returned from the server
   until the input stream is first accessed.
8. Using AssertJ the two input streams are checked for equality.

### Client properties

Properties can be provided via 2 routes, the url and properties. The DefaultClient specifies that the attributes on the url (query) take precedence over those in the provided property map.

| Name | Property | Query Parameter | Required | Description |
| --- | --- | --- | --- | --- |
| User ID | service.userid | userid | YES | The user ID is is used as part of a query to the server |
| Palisade Service Port | service.palisade.port | psport | NO | If provided will override any port provided on the Palisade URL provided to the session. |
| Filtered Resource Service Port | service.filteredResource.port | wsport | NO | If provided will override any port provided on the Palisade URL provided to the session. |

Some properties can be overriden, but for testing.

__Note__: These properties are not available as a querystring parameter.

| Name | Property | Default | Description |
| --- | --- | --- | --- |
| Palisade Service Path | service.palisade.path | palisade/api/registerDataRequest | the path which is appended to the Palisade service URL |
| Filtered Resource Service Path | service.filteredResource.path | resource/%t | the path which is appended to the Filtered Resource Service URL |
| Data Service Path | service.data.path | read/chunked | the path which is appended to the Data Service URL |

## Technologies Used

### Runtime

* [Immutables](https://immutables.github.io/) - Java annotation processors to generate simple, safe and consistent value objects.
* [Jackson]() - JSON for Java. Handles all the (de)serialisation of objects to/from the Palisade servers.

### Test Only

* [Junit5](https://junit.org/junit5/) - Needs no introduction :)
* [AssertJ](https://assertj.github.io/doc/) - Excellent testing library
* [Logback](http://logback.qos.ch/) - Great logging library. Used for testing.
* [Micronaut HTTP Server](https://micronaut.io/) - Used for testing

## Issues

There is currently a problem with Palisade regarding HTTP/2. For this reason, the client is currently limited to HTTP/1.1 and will not request a protocol upgrade.
