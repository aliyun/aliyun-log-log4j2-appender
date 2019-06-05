# Log4j2 Appender

[![Build Status](https://travis-ci.org/aliyun/aliyun-log-log4j2-appender.svg?branch=master)](https://travis-ci.org/aliyun/aliyun-log-log4j2-appender)
[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[中文版README](/README_CN.md)

## Aliyun Log Log4j2 Appender

Apache Log4j2 is an upgrade to Log4j that provides significant improvements over its predecessor, Log4j 1.x. You can control the destination of the log through Log4j2. It can be console, file, GUI components, socket, NT event log, syslog. You can control the output format for each log as well. You can control the generation process of the log through log level. The most interesting thing is you can complete the above things through a configuration file and without any code modification.

You can set the destination of your log to AliCloud Log Service through `Aliyun Log Log4j2 Appender`. The format of the log in AliCloud Log Service is as follows:
```
level: ERROR
location: com.aliyun.openservices.log.logback.example.LogbackAppenderExample.main(LogbackAppenderExample.java:18)
message: error log
throwable: java.lang.RuntimeException: xxx
thread: main
time: 2018-01-02T03:15+0000
log: 2018-01-02 11:15:29,682 ERROR [main] com.aliyun.openservices.log.logback.example.LogbackAppenderExample: error log
__source__: xxx
__topic__: yyy
```
Field Specifications:
+ `level` stands for log level
+ `location` is logs's output position
+ `message` is the content of the log
+ `throwable` is exception of the log (this field will appear only if the exception is recorded)
+ `thread` stands for thread name
+ `time` is the log's generation time (you can configure it's format through timeFormat and timeZone)
+ `log` is custom log format (this field will appear only if you configure the layout)
+ `__source__` is the log's source, you can specify its value in conf file
+ `__topic__` is the log's topic, you can specify its value in conf file

## Advantage
+ `Disk Free`: the generation data will be send to AliCloud Log Service in real time through network.
+ `Without Refactor`: if your application already use Log4j2, you can just add Log4j2 appender to your configuration file.
+ `Asynchronous and High Throughput`: the data will be send to AliCloud Log Service asynchronously. It is suitable for high concurrent write.
+ `Context Query`: at server side, in addition to searching log with keywords, you can obtain the context information of original log as well.


## Supported Version
* aliyun-log-producer 0.2.0
* protobuf-java 2.5.0

> This version is mainly suitable for Log4j 2.X versions. For Log4j 1.x, please refer to
[aliyun-log-log4j-appender](https://github.com/aliyun/aliyun-log-log4j-appender)


## Configuration Steps

### 1. Adding the Dependencies in pom.xml

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>2.5.0</version>
</dependency>
<dependency>
    <groupId>com.aliyun.openservices</groupId>
    <artifactId>aliyun-log-log4j2-appender</artifactId>
    <version>0.1.11</version>
</dependency>
```

### 2. Modify the Configuration File

Take `log4j2.xml` as an example, you can configure the appender and logger related to AliCloud Log Services as follows:
```
<Appenders>
    <Loghub name="Loghub"
            project="your project"
            logStore="your logStore"
            endpoint="your project endpoint"
            accessKeyId="your accessKey id"
            accessKeySecret="your accessKey secret"
            totalSizeInBytes="104857600"
            maxBlockMs="60000"
            ioThreadCount="8"
            batchSizeThresholdInBytes="524288"
            batchCountThreshold="4096"
            lingerMs="2000"
            retries="10"
            baseRetryBackoffMs="100"
            maxRetryBackoffMs="100"
            topic="your topic"
            source="your source"
            timeFormat="yyyy-MM-dd'T'HH:mmZ"
            timeZone="UTC"
            ignoreExceptions="true">
        <PatternLayout pattern="%d %-5level [%thread] %logger{0}: %msg"/>
    </Loghub>
</Appenders>
<Loggers>
    <Root level="warn">
        <AppenderRef ref="Loghub"/>
    </Root>
</Loggers>
```

## Parameter Description

The `Aliyun Log Log4j2 Appender` provides the following parameters.
```
# Specify the project name of your log services, required
project = [your project]
# Specify the logstore of your log services, required
logStore = [your logStore]
# Specify the HTTP endpoint of your log services, required
endpoint = [your project endpoint]
# Specify the account information of your log services, required
accessKeyId = [your accesskey id]
accessKeySecret = [your accessKeySecret]

# The upper limit log size that a single producer instance can hold, default is 100MB.
totalSizeInBytes=104857600
# If the producer has insufficient free space, the caller's maximum blocking time on the send method, defaults is 60 seconds.
maxBlockMs=60
# The thread pool size for executing log sending tasks, defaults is the number of processors available.
ioThreadCount=8
# When the size of the cached log in a Producer Batch is greater than or equal batchSizeThresholdInBytes, the batch will be send, default is 512KB, maximum can be set to 5MB.
batchSizeThresholdInBytes=524288
# When the number of log entries cached in a ProducerBatch is greater than or equal to batchCountThreshold, the batch will be send.
batchCountThreshold=4096
# A ProducerBatch has a residence time from creation to sending, defaulting is 2 seconds and a minimum of 100 milliseconds.
lingerMs=2000
# The number of times a Producer Batch can be retried if it fails to send for the first time, default is 10.
retries=10
# The backoff time for the first retry, default 100 milliseconds.
baseRetryBackoffMs=100
# The maximum backoff time for retries, default is 50 seconds.
maxRetryBackoffMs=100

# Specify the topic of your log, default is "", optional
topic = [your topic]

# Specify the source of your log, default is host ip, optional
source = [your source]

# Specify time format of the field time, default is yyyy-MM-dd'T'HH:mm:ssZ, optional
timeFormat = yyyy-MM-dd'T'HH:mm:ssZ

# Specify timezone of the field time, default is UTC, optional
timeZone = UTC
```


## Sample Code

[Log4j2AppenderExample.java](/src/main/java/com/aliyun/openservices/log/log4j2/example/Log4j2AppenderExample.java)

[log4j2-example.xml](/src/main/resources/log4j2-example.xml)

## Troubleshooting

The `Aliyun Log Log4j2 Appender` will record the exceptions generated by log4j2 appender itself through `org.apache.logging.log4j.status.StatusLogger`. By default, log4j2 will register a listener named `StatusConsoleListener` for StatusLogger. So the exceptions generated by log4j2 appender will appear in console by default. When you encounter issue, please pay attention to the error message in console.

## Contributors
[@LNAmp](https://github.com/LNAmp) [@zzboy](https://github.com/zzboy) made a great contribution to this project.

Thanks for the excellent work by [@LNAmp](https://github.com/LNAmp) [@zzboy](https://github.com/zzboy).
