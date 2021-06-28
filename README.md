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

# Palisade Clients

## Client Implementations

The job of the client code is to send the request for data into Palisade and to interpret the result as required for the data processing technology it is written for.
The responsibility for implementations of the client code is to provide users with a way to request data from Palisade in a way that the user has to make minimal changes to how they would normally use that processing technology.
Implementations of this component will usually require deep understanding of the data processing technology in order to best hook into that technology, without needing to fork the code for that technology.

To find out more information on our technology specific clients, please see the following modules:
* [Akka Client](client-akka/README.md)
* [Fuse Client](client-fuse/README.md)
* [Java Client](client-java/README.md)
* [Shell Client](client-shell/README.md)


### Prerequisites
1. [Git](https://git-scm.com/)
2. [Maven](https://maven.apache.org/)

The examples may have additional prerequisites

<span style="color:red">
We do not currently support Windows as a build environment, If you are running on Windows then you will need this: Microsoft Visual C++ 2010 SP1 Redistributable Package
</span>


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
mvn install
```


## License

Palisade-clients is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).

## Contributing
We welcome contributions to the project. Detailed information on our ways of working can be found [here](https://gchq.github.io/Palisade/doc/other/ways_of_working.html).

## FAQ

Q: What versions of Java are supported?  
A: We are currently using Java 11.
