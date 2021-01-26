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
# Cat Client

The cat client is designed to provide a capability similar to the linux terminal 'cat' command for printing out to the terminal the contents of a file.

To use the cat client you will need to have a running deployment of Palisade.  An example would be to run through the local JVM example:

```bash
git clone https://github.com/gchq/Palisade-examples
```
see the Readme at ~/Palisade-examles/example/deployment/local-jvm/README.md but instead of running the runLocalJVMExample.sh, you should run the following command: 

```bash
PALISADE_REST_CONFIG_PATH="$(pwd)/example/example-model/src/main/resources/configRest.json" java -cp $(pwd)/client-impl/cat-client/target/cat-client-*-shaded.jar CatClient Alice file://$(pwd)/example/resources/employee_file0.avro SALARY
```

That command makes it easy for a system admin to create an alias for users such that it hides most of the complication of the command and just leaves the user to specify the resource to access and the purpose for accessing it. For example the system administrator could set the following alias:
```bash
alias cat="PALISADE_REST_CONFIG_PATH=$(pwd)/example/example-model/src/main/resources/configRest.json java -cp $(pwd)/client-impl/cat-client/target/cat-client-*-shaded.jar CatClient "'$(whoami)'
```

Then a user could run the command:
```bash
cat file://$(pwd)/example/resources/employee_file0.avro SALARY
```