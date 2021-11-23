# Log4j2 Appender

[![Build Status](https://travis-ci.org/aliyun/aliyun-log-log4j2-appender.svg?branch=master)](https://travis-ci.org/aliyun/aliyun-log-log4j2-appender)
[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[README in English](/README.md)

## Aliyun Log Log4j2 Appender
Log4j2 是 log4j 的升级版本。通过使用 Log4j2，您可以控制日志信息输送的目的地是控制台、文件、GUI 组件、甚至是套接口服务器、NT 的事件记录器、UNIX Syslog 守护进程等；您也可以控制每一条日志的输出格式；通过定义每一条日志信息的级别，您能够更加细致地控制日志的生成过程。最令人感兴趣的就是，这些可以通过一个配置文件来灵活地进行配置，而不需要修改应用的代码。

通过 Aliyun Log Log4j2 Appender，您可以控制日志的输出目的地为阿里云日志服务。需要注意的是，Aliyun Log Log4j2 Appender 不支持设置日志的输出格式，写到日志服务中的日志的样式如下：
```
level: ERROR
location: com.aliyun.openservices.log.log4j2.example.Log4j2AppenderExample.main(Log4j2AppenderExample.java:16)
message: error log
throwable: java.lang.RuntimeException: xxx
thread: main
time: 2018-01-02T03:15+0000
log: 2018-01-02 11:15:29,682 ERROR [main] com.aliyun.openservices.log.log4j2.example.Log4j2AppenderExample: error log
__source__: xxx
__topic__: yyy
```
其中：
+ level 日志级别。
+ location 日志打印语句的代码位置。
+ message 日志内容。
+ throwable 日志异常信息（只有记录了异常信息，这个字段才会出现）。
+ thread 线程名称。
+ time 日志打印时间（可以通过 timeFormat 或 timeZone 配置 time 字段呈现的格式和时区）。
+ log 自定义日志格式（只有设置了 encoder，这个字段才会出现）。
+ \_\_source\_\_ 日志来源，用户可在配置文件中指定。
+ \_\_topic\_\_ 日志主题，用户可在配置文件中指定。


## 功能优势
+ 日志不落盘：产生数据实时通过网络发给服务端。
+ 无需改造：对已使用 Log4j2 应用，只需简单配置即可采集。
+ 异步高吞吐：高并发设计，后台异步发送，适合高并发写入。
+ 上下文查询：服务端除了通过关键词检索外，给定日志能够精确还原原始日志文件上下文日志信息。


## 版本支持
* aliyun-log-producer 0.3.9
* protobuf-java 2.5.0

> 该版本主要适配于Log4J 2.X以上版本，以下版本请参考[aliyun-log-log4j-appender](https://github.com/aliyun/aliyun-log-log4j-appender)


## 配置步骤

### 1. maven 工程中引入依赖

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>2.5.0</version>
</dependency>
<dependency>
    <groupId>com.aliyun.openservices</groupId>
    <artifactId>aliyun-log-log4j2-appender</artifactId>
    <version>0.1.12</version>
</dependency>
```

### 2. 修改配置文件

以xml型配置文件`log4j2.xml`为例（不存在则在项目根目录创建），配置 Loghub 相关的 appender 与 Logger，例如：
```
<Appenders>
    <Loghub name="Loghub"
            project="your project"
            logStore="your logStore"
            endpoint="your project endpoint"
            accessKeyId="your accessKey id"
            accessKeySecret="your accessKey secret"
            totalSizeInBytes="104857600"
            maxBlockMs="0"
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
其中：
level 是日志记录的优先级，优先级从高到低分别是 ERROR、WARN、INFO、DEBUG。通过在这里定义的级别，您可以控制应用程序中相应级别的日志信息的开关。比如在这里定义了 WARN 级别，则应用程序中所有 INFO、DEBUG
级别的日志信息将不被打印出来。

## 参数说明

Aliyun Log Log4j2 Appender 可供配置的属性（参数）如下，其中注释为必选参数的是必须填写的，可选参数在不填写的情况下，使用默认值。

```
#日志服务的 project 名，必选参数
project = [your project]
#日志服务的 logstore 名，必选参数
logStore = [your logStore]
#日志服务的 HTTP 地址，必选参数
endpoint = [your project endpoint]
#用户身份标识，必选参数
accessKeyId = [your accesskey id]
accessKeySecret = [your accessKeySecret]

#单个 producer 实例能缓存的日志大小上限，默认为 100MB。
totalSizeInBytes=104857600
#如果 producer 可用空间不足，调用者在 send 方法上的最大阻塞时间，默认为 0 秒。为了不阻塞打印日志的线程，强烈建议将该值设置成 0。
maxBlockMs=0
#执行日志发送任务的线程池大小，默认为可用处理器个数。
ioThreadCount=8
#当一个 ProducerBatch 中缓存的日志大小大于等于 batchSizeThresholdInBytes 时，该 batch 将被发送，默认为 512 KB，最大可设置成 5MB。
batchSizeThresholdInBytes=524288
#当一个 ProducerBatch 中缓存的日志条数大于等于 batchCountThreshold 时，该 batch 将被发送，默认为 4096，最大可设置成 40960。
batchCountThreshold=4096
#一个 ProducerBatch 从创建到可发送的逗留时间，默认为 2 秒，最小可设置成 100 毫秒。
lingerMs=2000
#如果某个 ProducerBatch 首次发送失败，能够对其重试的次数，默认为 10 次。
#如果 retries 小于等于 0，该 ProducerBatch 首次发送失败后将直接进入失败队列。
retries=10
#该参数越大能让您追溯更多的信息，但同时也会消耗更多的内存。
maxReservedAttempts=11
#首次重试的退避时间，默认为 100 毫秒。
#Producer 采样指数退避算法，第 N 次重试的计划等待时间为 baseRetryBackoffMs * 2^(N-1)。
baseRetryBackoffMs=100
#重试的最大退避时间，默认为 50 秒。
maxRetryBackoffMs=100

#指定日志主题，默认为 ""，可选参数
topic = [your topic]

#指的日志来源，默认为应用程序所在宿主机的 IP，可选参数
source = [your source]

#输出到日志服务的时间的格式，默认是 yyyy-MM-dd'T'HH:mm:ssZ，可选参数
timeFormat = yyyy-MM-dd'T'HH:mm:ssZ

#输出到日志服务的时间的时区，默认是 UTC，可选参数（如果希望 time 字段的时区为东八区，可将该值设定为 Asia/Shanghai）
timeZone = UTC
```
参阅：https://github.com/aliyun/aliyun-log-producer-java

## 使用实例
项目中提供了一个名为`com.aliyun.openservices.log.log4j2.Log4j2AppenderExample`的实例，它会加载 resources 目录下的`log4j2.xml`文件进行log4j2配置。

**log4j2.xml样例说明**
+ 配置了三个appender：loghubAppender1、loghubAppender2、STDOUT。
+ loghubAppender1：将日志输出到project=test-proj，logstore=store1。输出WARN、ERROR级别的日志。
+ loghubAppender2：将日志输出到project=test-proj，logstore=store2。只输出INFO级别的日志。
+ STDOUT：将日志输出到控制台。由于没有对日志级别进行过滤，会输出root中配置的日志级及以上的所有日志。

[Log4j2AppenderExample.java](/src/main/java/com/aliyun/openservices/log/log4j2/example/Log4j2AppenderExample.java)

[log4j2-example.xml](/src/main/resources/log4j2-example.xml)

## 错误诊断

如果您发现数据没有写入日志服务，可通过如下步骤进行错误诊断。
1. 检查配置文件 log4j2.xml 是否限定了 appender 只输出特定级别的日志。比如，是否设置了 root，logger 或 appender 的 level 属性，是否在 appender 中设使用了 [filter](https://logging.apache.org/log4j/2.0/manual/filters.html)。
2. 检查您项目中引入的 protobuf-java，aliyun-log-log4j2-appender 这两个 jar 包的版本是否和文档中`maven 工程中引入依赖`部分列出的 jar 包版本一致。
3. 通过观察控制台的输出来诊断您的问题。Aliyun Log Log4j2 Appender 会将 appender 运行过程中产生的异常通过 `org.apache.logging.log4j.status.StatusLogger` 记录下来，默认情况下 log4j2 框架会为 StatusLogger 注册一个 StatusConsoleListener，因此 Aliyun Log Log4j2 Appender 自己运行过程中产生的异常会在默认情况下会输出到控制台。

## 常见问题

**Q**：是否支持自定义 log 格式？

**A**：在 0.1.9 及以上版本新增了 log 字段。您可以通过配置 layout 来自定义 log 格式，例如：
```
<PatternLayout pattern="%d %-5level [%thread] %logger{0}: %msg"/>
```
log 输出样例：
```
log:  2018-07-15 21:12:29,682 INFO [main] TestAppender: info message.
```

**Q**: 如何采集宿主机 IP？

**A**: 不要在 log4j2.xml 中设置 source 字段的值，这种情况下 source 字段会被设置成应用程序所在宿主机的 IP。

**Q**：用户可以自定义 source 字段的取值吗？

**A**：0.1.6 以及之前的版本不支持，在这些版本中 source 字段会被设置成应用程序所在宿主机的 IP。在最新的版本中，您可以参考上面的配置文件指定 source 的取值。

**Q**：在网络发生异常的情况下，`aliyun-log-log4j2-appender` 会如何处理待发送的日志？

**A**：`aliyun-log-log4j2-appender` 底层使用 `aliyun-log-producer-java` 发送数据。producer 会根据您在配置文件中设置的 `retryTimes` 进行重试，如果超过 `retryTimes` 次数据仍没有发送成功，会将错误信息输出，并丢弃该条日志。关于如何查看错误输出，可以参考错误诊断部分。

**Q**：如何关闭某些类输出的日志？

**A**：通过在 log4j2.xml 文件中添加 `<Logger name="packname" level="OFF"/>` 可屏蔽相应包下日志的输出。
例如，当您在 log4j2.xml 文件中添加如下内容会屏蔽 package 名为 `com.aliyun.openservices.log.producer.inner` 下所有类的日志输出。
```
 <Loggers>
    <Root level="DEBUG">
        <AppenderRef ref="Loghub"/>
    </Root>
    <Logger name="com.aliyun.openservices.log.producer.inner" level="OFF"/>
</Loggers>
```

**Q**：如果想设置 `time` 字段的时区为东八区或其他时区，该如何指定 `timeZone` 的取值？

**A**：当您将 `timeZone` 指定为 `Asia/Shanghai` 时，`time` 字段的时区将为东八区。timeZone 字段可能的取值请参考 [java-util-timezone](http://tutorials.jenkov.com/java-date-time/java-util-timezone.html)。

**Q**：如果想在Spring Boot 2 中引入 aliyun-log-log4j2-appender？

**A**：使用以下依赖:
```
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.springframework</groupId>
                <artifactId>spring-jcl</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>
    <dependency>
        <groupId>com.google.protobuf</groupId>
        <artifactId>protobuf-java</artifactId>
        <version>2.5.0</version>
    </dependency>
    <dependency>
        <groupId>com.aliyun.openservices</groupId>
        <artifactId>aliyun-log-log4j2-appender</artifactId>
        <version>0.1.12</version>
    </dependency>
```

**Q**：为什么程序在运行时会抛出`java.lang.InterruptedException`？

**A**：aliyun-log-log4j2-appender 会调用 [Producer.send()](https://github.com/aliyun/aliyun-log-java-producer/blob/master/src/main/java/com/aliyun/openservices/aliyun/log/producer/Producer.java#L16) 方法发送数据。执行 send() 方法的线程如果被中断了，如调用了 Thread.interrupted() 方法，就会抛出这样的异常。
调用  [Producer.send()](https://github.com/aliyun/aliyun-log-java-producer/blob/master/src/main/java/com/aliyun/openservices/aliyun/log/producer/Producer.java#L16) 方法所属的线程和您调用 LOGGER.info() 打印日志的线程是相同的线程，请检查您的程序在何时会调用 Thread.interrupted()  方法。

## 贡献者
[@LNAmp](https://github.com/LNAmp) [@zzboy](https://github.com/zzboy) 对项目作了很大贡献。

感谢 [@LNAmp](https://github.com/LNAmp) [@zzboy](https://github.com/zzboy) 的杰出工作。
