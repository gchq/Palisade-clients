<!--
/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
-->

# <img src="logos/logo.svg" width="180">

## A Tool for Complex and Scalable Data Access Policy Enforcement
Windows is not an explicitly supported environment, although where possible Palisade has been made compatible.  
For Windows developer environments, we recommend setting up [WSL](https://docs.microsoft.com/en-us/windows/wsl/).

## Getting started
For an overview of Palisade, start with the Palisade introduction and the accompanying guides: QuickStart Guide; and Developer Guide: [Palisade README](https://github.com/gchq/Palisade).

# Palisade Clients

## Client Implementations
The Client implementations will need to send requests into Palisade and process the response.  
The workflow for this is expected to follow the pattern of first sending in a request to register access for data.  
This will return a response consisting of a token (a unique id) that can then be used in a second request to retrieve the actual data.
An implementation of a Client will need to conform to this workflow, but the technology that is used will be best suited to the intended use of the application.

The following examples demonstrate the use of different kinds of clients that can operate with the Palisade service:
* [Java Client](client-java/README.md)
  Java based RESTFul client using an API similar to the JDBC  
* [Shell Client](client-shell/README.md)
  Command Line Interface(CLI) client which provides interactive operations.
* [Fuse Client](client-fuse/README.md)
  Filesystem in Userspace (FUSE) client which uses a CLI for interactive operations and stores the results in a FUSE filesystem.
  This client is an extension of the Java Client.
* [Akka Client](client-akka/README.md)
  Akka client which provides access to an interface using both Java and Akka types.
  Client is an extension of the Java Client.
* [S3 Client](client-s3/README.md)
  Akka Microservice which provides an endpoint for accessing resources stored in an Amazon Web Service (AWS) Simple Cloud Storage (S3) bucket.  
  The example uses [Apache Spark](https://sparkjava.com/) for RESTful queries with the microservice. 

### Prerequisites
1. [Git](https://git-scm.com/)
2. [Maven](https://maven.apache.org/)


## Getting started

To get started, clone the Palisade Clients repository: 

```bash
git clone https://github.com/gchq/Palisade-clients.git
cd Palisade-clients
```

You should see the following modules:
```bash
>> ls
 drwxrwxrwx client-akka
 drwxrwxrwx client-fuse
 drwxrwxrwx client-java
 drwxrwxrwx client-shell
```
Now you can finally build the repository by running: 
```bash
mvn clean install
```

## License

Palisade-clients is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).

## Contributing
We welcome contributions to the project. Detailed information on our ways of working can be found [here](https://gchq.github.io/Palisade/doc/other/ways_of_working.html).
