# Log4j2 Appender

[![Build Status](https://travis-ci.org/aliyun/aliyun-log-log4j2-appender.svg?branch=master)](https://travis-ci.org/aliyun/aliyun-log-log4j2-appender)
[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[中文版README](/README_CN.md)

## Aliyun Log Log4j2 Appender

Apache Log4j2 is an upgrade to Log4j that provides significant improvements over its predecessor, Log4j 1.x. You can control the destination of the log through Log4j2. It can be console, file, GUI components, socket, NT event log, syslog. You can control the output format for each log as well. You can control the generation process of the log through log level. The most interesting thing is you can complete the above things through a configuration file and without any code modification.

You can set the destination of your log to AliCloud Log Service through `Aliyun Log Log4j2 Appender`. But it is important to note that `Aliyun Log Log4j2 Appender` doesn't support cofigure log's output format. The format of the log in AliCloud Log Service is as follows:
```
level: ERROR
location: com.aliyun.openservices.log.log4j2.example.Log4j2AppenderExample.main(Log4j2AppenderExample.java:18)
message: error log
thread: main
time: 2018-01-02T03:15+0000
```
Field Specifications:
+ `level` stands for log level
+ `location` is logs's output position
+ `message` is the content of the log
+ `thread` stands for thread name
+ `time` is the log's generation time


## Advantage
+ `Disk Free`: the generation data will be send to AliCloud Log Service in real time through network.
+ `Without Refactor`: if your application already use Log4j2, you can just add Log4j2 appender to your configuration file.
+ `Asynchronous and High Throughput`: the data will be send to AliCloud Log Service asynchronously. It is suitable for high concurrent write.
+ `Context Query`: at server side, in addition to searching log with keywords, you can obtain the context information of original log as well.


## Supported Version
* log-loghub-producer 0.1.10
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
    <version>0.1.3</version>
</dependency>
```

### 2. Modify the Configuration File

Take `log4j2.xml` as an example, you can configure the appender and logger related to AliCloud Log Services as follows:
```
<Appenders>
    <Loghub name="Loghub"
            projectName="your project"
            logstore="your logstore"
            endpoint="your project endpoint"
            accessKeyId="your accesskey id"
            accessKey="your accesskey"
            packageTimeoutInMS="3000"
            logsCountPerPackage="4096"
            logsBytesPerPackage="3145728"
            memPoolSizeInByte="104857600"
            retryTimes="3"
            maxIOThreadSizeInPool="8"
            topic="your topic"
            timeFormat="yyyy-MM-dd'T'HH:mmZ"
            timeZone="UTC"
            ignoreExceptions="true">
        <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
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
projectName = [your project]
# Specify the logstore of your log services, required
logstore = [your logstore]
# Specify the HTTP endpoint of your log services, required
endpoint = [your project endpoint]
# Specify the account information of your log services, required
accessKeyId = [your accesskey id]
accessKey = [your accesskey]

# Specify the timeout for sending package, in milliseconds, default is 3000, the lower bound is 10, optional
packageTimeoutInMS = 3000
# Specify the maximum log count per package, the upper limit is 4096, optional
logsCountPerPackage = 4096
# Specify the maximum cache size per package, the upper limit is 5MB, in bytes, optional
logsBytesPerPackage = 5242880
# The upper limit of the memory that can be used by appender, in bytes, default is 100MB, optional
memPoolSizeInByte = 1048576000
# Specify the I/O thread pool's maximum pool size, the main function of the I/O thread pool is to send data, default is 8, optional
maxIOThreadSizeInPool = 8
# Specify the retry times when failing to send data, if exceeds this value, the appender will record the failure message to BasicStatusManager, default is 3, optional
retryTimes = 3

# Specify the topic of your log
topic = [your topic]

# Specify the time format of the data being sent to AliCloud Log Service, use SimpleDateFormat in Java to format time, default is ISO8601，optional
timeFormat = yyyy-MM-dd'T'HH:mmZ
timeZone  UTC
```


## Sample Code

[Log4j2AppenderExample.java](/src/main/java/com/aliyun/openservices/log/log4j2/example/Log4j2AppenderExample.java)

[log4j2.xml](/src/main/resources/log4j2.xml)

## Contributors
[@LNAmp](https://github.com/LNAmp) [@zzboy](https://github.com/zzboy) made a great contribution to this project.

Thanks for the excellent work by [@LNAmp](https://github.com/LNAmp) [@zzboy](https://github.com/zzboy).