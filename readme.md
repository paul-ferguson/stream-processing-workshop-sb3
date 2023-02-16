# Stream Processing Workshop SB3
This is the Stream Processing Workshop upgraded to Spring Boot 3 to enable native support.

Upgraded to Spring Boot 3.0.2.
```
id 'org.springframework.boot' version '3.0.2'
```

Added native support: org.graalvm.buildtools.native
```
    id 'org.graalvm.buildtools.native' version '0.9.18'
```

Removed the specific org.apache.kafka:kafka-streams version and let Spring Boot choose it.
Went from 7.3.0-ce to 3.3.2, but now all the classes are defined when running natively.
However, now at startup we see an `inStream parameter is null` exception caused by https://issues.apache.org/jira/browse/KAFKA-14270 that was fixed in 3.4.0.

Updated the default value serde class as I don't think `org.springframework.kafka.support.serializer.JsonSerde` is valid.
```
props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
```

Needed to convert to a more standard Spring Boot application configuration by adding Application, KafkaConfig and Processor classes.
The KafkaConfig class replaced the Streams class.
This then fixes some errors and allows Spring Boot manage the stream execution.

Run the gradle task nativeCompile. This uses your JAVA_HOME to run the `gu` command so verify its set correctly.
Once nativeCompile has completed it will generate an executable at build/native/nativeCompile/stream-processing-workshop-sb3.

## Issues
When streams needs to use the RocksDB it is not found in native.
```
Caused by: java.lang.RuntimeException: librocksdbjni-osx.jnilib was not found inside JAR.
	at org.rocksdb.NativeLibraryLoader.loadLibraryFromJarToTemp(NativeLibraryLoader.java:125) ~[na:na]
	at org.rocksdb.NativeLibraryLoader.loadLibraryFromJar(NativeLibraryLoader.java:102) ~[na:na]
	at org.rocksdb.NativeLibraryLoader.loadLibrary(NativeLibraryLoader.java:82) ~[na:na]
	at org.rocksdb.RocksDB.loadLibrary(RocksDB.java:68) ~[stream-processing-workshop-sb3:na]
	at org.rocksdb.RocksDB.<clinit>(RocksDB.java:37) ~[stream-processing-workshop-sb3:na]
```