# 版本支持

- log-loghub-producer 0.1.8
- protobuf-java 2.5.0

 > 该版本主要适配于Log4J 2.X以上版本，以下版本请参考[aliyun-log-log4j-appender](https://github.com/aliyun/aliyun-log-log4j-appender)


# 配置步骤

1. maven 工程中引入依赖

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>2.5.0</version>
</dependency>
<dependency>
     <groupId>com.aliyun.openservices</groupId>
     <artifactId>aliyun-log-log4j2-appender</artifactId>
     <version>0.1.0</version>
</dependency>
```
2. 修改配置文件：以xml型配置文件log4j2.xml为例（不存在则在项目根目录创建），配置Loghub相关的appender与 Logger，例如：

```
    <Appenders>
        <Loghub name="Loghub"
                projectName="your project"
                logstore="your logstore"
                endpoint="your project endpoint"
                accessKeyId="your accesskey id"
                accessKey="your accesskey"
                stsToken="your ststoken"
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
其中：
level 是日志记录的优先级，优先级从高到低分别是 ERROR、WARN、INFO、DEBUG。通过在这里定义的级别，您可以控制应用程序中相应级别的日志信息的开关。比如在这里定义了 WARN 级别，则应用程序中所有 INFO、DEBUG
级别的日志信息将不被打印出来。

3. 参数说明

   Loghub Log4j2 Appender 可供配置的属性（参数）如下，其中注释为必选参数的是必须填写的，可选参数在不填写的情况下，使用默认值。

```
#日志服务的 project 名，必选参数
projectName = [your project]
#日志服务的 logstore 名，必选参数
logstore = [your logstore]
#日志服务的 HTTP 地址，必选参数
endpoint = [your project endpoint]
#用户身份标识，必选参数
accessKeyId = [your accesskey id]
accessKey = [your accesskey]
#当使用临时身份时必须填写，非临时身份则删掉这行配置
stsToken = [your ststoken]

#被缓存起来的日志的发送超时时间，如果缓存超时，则会被立即发送，单位是毫秒，默认值为3000，最小值为10，可选参数
packageTimeoutInMS = 3000
#每个缓存的日志包中包含日志数量的最大值，不能超过 4096，可选参数
logsCountPerPackage = 4096
#每个缓存的日志包的大小的上限，不能超过 5MB，单位是字节，可选参数
logsBytesPerPackage = 5242880
#Appender 实例可以使用的内存的上限，单位是字节，默认是 100MB，可选参数
memPoolSizeInByte = 1048576000
#指定I/O线程池最大线程数量，主要用于发送数据到日志服务，默认是8，可选参数
maxIOThreadSizeInPool = 8
#指定发送失败时重试的次数，如果超过该值，会把失败信息记录到log4j的StatusLogger里，默认是3，可选参数
retryTimes = 3

#指定日志主题
topic = [your topic]

#输出到日志服务的时间格式，使用 Java 中 SimpleDateFormat 格式化时间，默认是 ISO8601，可选参数
timeFormat = yyyy-MM-dd'T'HH:mmZ
timeZone  UTC
```
参阅：https://help.aliyun.com/document_detail/43758.html
