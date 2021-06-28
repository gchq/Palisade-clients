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

# Palisade Shell

The Palisade Shell provides an interactive CLI REPL for access to a cluster and its resources.
Due to the interactive nature of the shell, it is recommended to get familiar with the example setup from [Palisade examples](https://github.com/gchq/Palisade-examples/), and the [example-library](https://github.com/gchq/Palisade-examples/tree/develop/example-library) in particular.

> :information_source:
In the case of the example-library, the `context` is limited to just a `purpose` - one of `SALARY`, `DUTY_OF_CARE`, or `STAFF_REPORT`.
Additionally, there are a number of pre-populated users (`Alice`, `Bob`, and `Eve`), resources (the pair of files `/data/local-data-store/employee_file{0,1}.avro`) and rules.
All examples here are therefore written to be compliant with this example pre-populated data.

## Sample Extract

Alice wants to read some 'Employee' data because she is doing salary review for the corporation.
She connects to an instance of Palisade running at `192.168.49.2:30094` (a local k8s deployment).
The file is in `avro` format, which must be further deserialised if she is to make further use of it.
The deserialised data will be redacted in-line with corporate policy for userId `Alice`, purpose `SALARY`, and rules for the requested resources.

See the following extract from the session using the shell, demonstrated with the example-library data:
```shell script
no-one@disconnected> connect pal://192.168.49.2:30094/?userid=Alice
Connected to pal://192.168.49.2:30094/?userid=Alice

Alice@192.168.49.2> register purpose=SALARY file:/data/local-data-store/
6b6784e4-2383-40ac-92dc-7daa3b0a67b4

Alice@192.168.49.2> cd 6b6784e4-2383-40ac-92dc-7daa3b0a67b4
Selected 6b6784e4-2383-40ac-92dc-7daa3b0a67b4

Alice@192.168.49.2#6b6784e4-2383-40ac-92dc-7daa3b0a67b4> ls
file:/data/local-data-store/employee_file1.avro
file:/data/local-data-store/employee_file0.avro

Alice@192.168.49.2#6b6784e4-2383-40ac-92dc-7daa3b0a67b4> cat file:/data/local-data-store/employee_file0.avro
Objavro.schema`{"type":"record","name":"Employee","namespace":"uk.gov.gchq.synt[+ 18086 characters]
```


## List of Commands
Some effort has been made to make the Shell appear somewhat similar to a UNIX shell (bash, etc.).
Subsequently, some commands have aliases to their UNIX counterparts.

### Default Commands - `help`, `exit`
Spring-shell-starter bundles some default commands, such as `help` and `exit`.
Use the `help` command to list all available commands and their one-liner doc-string.
Use the `exit` command to exit the shell.

### Connect - `connect`
Configure the backing [Java Client](../client-java/README.md) to point to a cluster address, as well as setting the `userId`.

Suppose the target instance of Palisade is running locally in K8s and the ingress is exposed at `192.168.49.2:30094`, and we want to issue a request for data as "Alice", then our connect command would be:
```shell script
disconnected> connect pal://192.168.49.2:30094/?userid=Alice
Connected to pal://192.168.49.2:30094/?userid=Alice
```
No checks are necessarily made that the server is available at this point, it is purely configuration for the client.
This is described more in-depth in the [URL configuration](../client-java/README.md#URL) and [client properties](../client-java/README.md#Client%20properties) sections of the [client-java README](../client-java/README.md).
For most cases, the URL looks like`pal://[ingressAddress]/?userid=[userId]`

### Register - `register`
Given a `context` for accessing data, register a request for a `resourceId`.

Suppose the target resource is the directory `file:/data/local-data-store/`, then our register command would be:
```shell script
Alice@192.168.49.2> register purpose=SALARY file:/data/local-data-store/
6b6784e4-2383-40ac-92dc-7daa3b0a67b4
```

### List - `list`, `ls`
List either all registered requests and their tokens, or all resources returned for a given token token.
```shell script
Alice@192.168.49.2> ls
6b6784e4-2383-40ac-92dc-7daa3b0a67b4

Alice@192.168.49.2> ls 6b6784e4-2383-40ac-92dc-7daa3b0a67b4

file:/data/local-data-store/employee_file0.avro
file:/data/local-data-store/employee_file1.avro
```

### Read - `read`, `cat`

Suppose we want to read the file `file:/data/local-data-store/employee_file0.avro`, then our read command would be:
```shell script
Alice@192.168.49.2> cat file:/data/local-data-store/employee_file0.avro 6b6784e4-2383-40ac-92dc-7daa3b0a67b4
Objavro.schema`{"type":"record","name":"Employee","namespace":"uk.gov.gchq.synt[+ 18086 characters]
```

### Select - `select`, `cd`
Select a `token` to replace the need to enter it for subsequent commands.

```shell script
Alice@192.168.49.2> cd 6b6784e4-2383-40ac-92dc-7daa3b0a67b4
Selected 6b6784e4-2383-40ac-92dc-7daa3b0a67b4

Alice@192.168.49.2#6b6784e4-2383-40ac-92dc-7daa3b0a67b4> ls
file:/data/local-data-store/employee_file0.avro
file:/data/local-data-store/employee_file1.avro

Alice@192.168.49.2#6b6784e4-2383-40ac-92dc-7daa3b0a67b4> cat file:/data/local-data-store/employee_file0.avro
Objavro.schema`{"type":"record","name":"Employee","namespace":"uk.gov.gchq.synt[+ 18086 characters]
```
