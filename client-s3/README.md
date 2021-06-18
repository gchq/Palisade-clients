```scala
/* This is done implicitly when using spark-shell */
val spark = SparkSession.builder().getOrCreate()

spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "localhost:8092/request")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")

/* These are not interpreted or validated by Palisade, but Spark requires them non-null */
spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "Alice")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "alice's_secret")
```

```shell script
# User 'Alice' wants 'file:/data/local-data-store/' directory for 'SALARY' purposes
curl -X POST "http://localhost:8092/register?userId=Alice&resourceId=file%3A%2Fdata%2Flocal-data-store%2F&purpose=SALARY"
# We get back the token '09d3a677-3d03-42e0-8cdb-f048f3929f8c', to be used as a bucket-name
```

```scala
spark.read.format("avro").load("s3a://09d3a677-3d03-42e0-8cdb-f048f3929f8c/with-policy").show()
```
