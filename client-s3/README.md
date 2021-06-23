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

> :information_source: ***Work In Progress***
> There are some issues between what Spark expects and what the server returns for a `GetObject`.
> The server returns a valid AVRO object (which can be successfully read if written to a file).
> The server also returns what look to be all the correct headers.
> Despite this, a Spark `read` will return a data-frame with zero records.

# Palisade S3-Server Client

Presents an S3-compliant API wrapping a Palisade deployment.

Given a Spark job running against AWS S3 as follows:
```
spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "http://s3.eu-west-2.amazonaws.com/")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain")
val nonrecursive = scala.io.Source.fromFile("/schema/nonrecursive.json").mkString
spark.read.format("avro").option("avroSchema", nonrecursive).load("s3a://palisade-application-dev/data/remote-data-store/data/employee_file0.avro").show()
```
_Note that we use a modified non-recursive AVRO schema `/schema/nonrecursive.json` (this excludes the managers field) as recursive schema are not compatible with Spark SQL._

Adapt the Spark job to run against the Palisade S3 client:
```scala
import sys.process._;
// User 'Alice' wants 'file:/data/local-data-store/' directory for 'SALARY' purposes
// We get back the token '09d3a677-3d03-42e0-8cdb-f048f3929f8c', to be used as a bucket-name
val token = (Seq("curl", "-X", "POST", "http://localhost:8092/register?userId=Alice&resourceId=file%3A%2Fdata%2Flocal-data-store%2F&purpose=SALARY")!!).stripSuffix("\n")
Thread.sleep(5000)

spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "localhost:8092/request")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
// These are not interpreted or validated by Palisade, but Spark requires them to be non-null
spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "accesskey")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "secretkey")
// spark.read.format("avro").load("s3a://" + token + "/with-policy/employee_small.avro").show()
val nonrecursive = scala.io.Source.fromFile("/schema/nonrecursive.json").mkString
spark.read.format("avro").option("avroSchema", nonrecursive).option("mode", "FAILFAST").load("s3a://" + token + "/data/employee_file0.avro").show()
```

The client currently requires hard-coding the Palisade services URLs in the `EndpointConfiguration`.
