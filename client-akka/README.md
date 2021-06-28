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
# <img src="../logos/logo.svg" width="180">

## A Tool for Complex and Scalable Data Access Policy Enforcement

# Palisade Akka Client

The Akka Palisade Client API provides access to a Palisade cluster and exposes an interface using both Java standard-lib types and Akka types.
This exists alongside the [Java Client](../client-java) as both a demonstration of a different implementation, and to provide better compatibility with some Palisade internals, most of which make use of Akka.

## API Design

The client follows as simple an API as possible.
After providing configuration for the location of a cluster, the client is otherwise stateless and presents the flattest data-structure possible.
The methods and return types are a one-to-one mapping with each service required to interact with (`register` with Palisade Service, `fetch` from Filtered-Resource Service, `read` from Data Service).

## Technologies Used

* [Akka](https://akka.io/) streams and HTTP REST/websockets
* [Jackson](https://github.com/FasterXML/jackson) JSON parsing
