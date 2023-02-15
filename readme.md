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

Convert to a more standard Spring Boot Application.

Run the gradle task nativeCompile. This uses your JAVA_HOME to run the `gu` command so verify its set correctly.

Make sure there was data in the topic StreamTemplate uses data-demo-streams.

