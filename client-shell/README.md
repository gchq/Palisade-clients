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

# Palisade Shell

The Palisade Shell provides a CLI REPL for access to a cluster and its resources.

> :information_source:
In the case of the [Palisade examples](https://github.com/gchq/Palisade-examples/), the `context` is limited to just a `purpose` - one of `SALARY`, `DUTY_OF_CARE`, or `STAFF_REPORT`.
Additionally, there are a number of pre-populated users (`Alice`, `Bob`, and `Eve`), resources (the pair of files `/data/local-data-store/employee_file{0,1}.avro`) and rules.
All examples here are therefore written to be compliant with this example pre-populated data.


## Commands
Some effort has been made to make the Shell appear somewhat similar to a UNIX shell (bash, etc.).
Subsequently, some commands have aliases to their UNIX counterparts.

### Default Commands - `help`, `exit`
Spring-shell-starter bundles some default commands, such as `help` and `exit`.
Use the `help` command to list all available commands and their one-liner doc-string.
Use the `exit` command to exit the shell.

### Connect - `connect`
Configure the backing [Java Client](../client-java/README.md) to point to a cluster address, as well as setting the `userId`.

Suppose the target instance of Palisade is running locally in K8s and the ingress is exposed at `192.168.49.2:30042`, and we want to issue a request for data as "Alice", then our connect command would be:
```
disconnected> connect pal://192.168.49.2:30042/?userid=Alice
Connected to pal://192.168.49.2:30042/?userid=Alice

Alice@192.168.49.2> 
```
No checks are necessarily made that the server is available at this point, it is purely configuration for the client.
This is described more in-depth in the [URL configuration](../client-java/README.md#URL) and [client properties](../client-java/README.md#Client%20properties) sections of the [client-java README](../client-java/README.md).

### Register - `register`
Given a `context` for accessing data, register a request for a `resourceId`.

Suppose the target resource is the directory `file:/data/local-data-store/`, then our register command would be:
```
Alice@192.168.49.2> register purpose=SALARY,some-other=thing file:/data/local-data-store/
TODO-some-token-here
```

### List - `list`, `ls`
List either all registered requests and their tokens, or all resources returned for a given token token.

```
Alice@192.168.49.2> ls
TODO-some-token-here

Alice@192.168.49.2> ls TODO-some-token-here
file:/data/local-data-store/employee_file0.avro
file:/data/local-data-store/employee_file1.avro
```

### Read - `read`, `cat`

Suppose we want to read the file `file:/data/local-data-store/employee_file0.avro`, then our read command would be:
```
Alice@192.168.49.2> cat TODO-some-token-here file:/data/local-data-store/employee_file0.avro
Objavro.schema`{"type":"record","name":"Employee","namespace":"uk.gov.gchq.synt[+ 18086 characters]
```

### Select - `select`, `cd`
Select a `token` to replace the need to enter it for subsequent commands.

```
Alice@192.168.49.2> cd TODO-some-token-here
Selected TODO-some-token-here

Alice@192.168.49.2#TODO-some-token-here> ls
file:/data/local-data-store/employee_file0.avro
file:/data/local-data-store/employee_file1.avro

Alice@192.168.49.2#TODO-some-token-here> read file:/data/local-data-store/employee_file0.avro
Objavro.schema`{"type":"record","name":"Employee","namespace":"uk.gov.gchq.synt[+ 18086 characters]
```
