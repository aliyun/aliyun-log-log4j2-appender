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
+ `log` is custom log format (this field will appear only if you configure the encoder)
+ `__source__` is the log's source, you can specify its value in conf file
+ `__topic__` is the log's topic, you can specify its value in conf file

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
    <version>0.1.9</version>
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
# Specify the maximum cache size per package, the upper limit is 3MB, in bytes, optional
logsBytesPerPackage = 3145728
# The upper limit of the memory that can be used by appender, in bytes, default is 100MB, optional
memPoolSizeInByte = 1048576000
# Specify the I/O thread pool's maximum pool size, the main function of the I/O thread pool is to send data, default is 8, optional
maxIOThreadSizeInPool = 8
# Specify the retry times when failing to send data, if exceeds this value, the appender will record the failure message through it's StatusLogger, default is 3, optional
retryTimes = 3

# Specify the topic of your log
topic = [your topic]

# Specify the source of your log
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
