# Loghub Log4j2 Appender

Log4j是Apache的著名项目，随着Java应用的越来越广泛，对日志性能等方面的要求也越来越高。Log4j的升级版本Log4j2在前些年发布。Log4j2相比Log4j有很多功能和性能上的改进。

和Log4j一样，Log4j2也由三个重要的组件构成：日志信息的优先级，日志信息的输出目的地，日志信息的输出格式。日志信息的优先级从高到低有 ERROR、WARN、INFO、DEBUG，分别用来指定这条日志信息的重要程度；日志信息的输出目的地指定了日志将打印到控制台还是文件中；而输出格式则控制了日志信息的显示内容。

使用 Loghub Log4j2 Appender，您可以控制日志的输出目的地为阿里云日志服务，有一点需要特别注意，Loghub Log4j2 Appender 不支持设置日志的输出格式，写到日志服务中的日志的样式如下：

```
level:WARN
location:com.aliyun.openservices.log.log4j2.LoghubLog4j2AppenderTest.testLogCommonMessage(LoghubLog4j2AppenderTest.java:13)
message:This is a test common message logged by log4j2.
thread:main
time:2017-12-19T03:50+0000
```
其中：

level 是日志级别。
location 是日志打印语句的代码位置。
message 是日志内容。
thread 是线程名称。
time 是日志打印时间。
# Loghub Log4j2 Appender 的优势

客户端日志不落盘：即数据生产后直接通过网络发往服务端。
对于已经使用 log4j2 记录日志的应用，只需要简单修改配置文件就可以将日志传输到日志服务。
异步高吞吐，Loghub Log4j2 Appender 会将用户的日志 merge 之后异步发送，提高网络 IO 效率。

# 使用方法

Step 1： maven 工程中引入依赖。

```
<dependency>
     <groupId>com.aliyun.openservices</groupId>
     <artifactId>log-loghub-log4j2-appender</artifactId>
     <version>0.1.0</version>
</dependency>
```
Step 2: 修改log4j2配置文件，以xml型配置文件log4j2.xml为例（不存在则在项目根目录创建），配置Loghub相关的appender与 Logger，例如：

```
    <Appenders>
        <Loghub name="Loghub"
                projectName = "your project name"
                logstore = "your logstore"
                endpoint = "your endpoint"
                accessKeyId = "your accessKeyId"
                accessKey = "your accessKey">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Loghub>
    </Appenders>
    <Loggers>
        <Logger name="slsLogger" level="info">
            <AppenderRef ref="Loghub"/>
        </Logger>
        <Root level="warn">
            <AppenderRef ref="Loghub"/>
        </Root>
    </Loggers>
```
其中：

level 是日志记录的优先级，优先级从高到低分别是 ERROR、WARN、INFO、DEBUG。通过在这里定义的级别，您可以控制应用程序中相应级别的日志信息的开关。比如在这里定义了 INFO 级别，则应用程序中所有 DEBUG 级别的日志信息将不被打印出来。
使用 oghub log4j2 appender 的简单配置如下：
```
    <Appenders>
        <Loghub name="Loghub"
                projectName = "your project name"
                logstore = "your logstore"
                endpoint = "your endpoint"
                accessKeyId = "your accessKeyId"
                accessKey = "your accessKey">
        </Loghub>
    </Appenders>

```
配置中中括号内的部分是需要填写的，具体含义见下面的说明。

# 配置参数

Loghub Log4j2 Appender 可供配置的属性（参数）如下，其中注释为必选参数的是必须填写的，可选参数在不填写的情况下，使用默认值。

```
#日志服务的 project 名，必选参数
projectName = [you project]
#日志服务的 logstore 名，必选参数
logstore = [you logstore]
#日志服务的 HTTP 地址，必选参数
endpoint = [your project endpoint]
#用户身份标识，必选参数
accessKeyId = [your accesskey id]
accessKey = [your accesskey]
#当使用临时身份时必须填写，非临时身份则删掉这行配置
stsToken=[your ststoken]
#被缓存起来的日志的发送超时时间，如果缓存超时，则会被立即发送，单位是毫秒，默认值为3000，最小值为10，可选参数
packageTimeoutInMS=3000
#每个缓存的日志包中包含日志数量的最大值，不能超过 4096，可选参数
logsCountPerPackage=4096
#每个缓存的日志包的大小的上限，不能超过 5MB，单位是字节，可选参数
logsBytesPerPackage = 5242880
#Appender 实例可以使用的内存的上限，单位是字节，默认是 100MB，可选参数
memPoolSizeInByte=1048576000
#后台用于发送日志包的 IO 线程的数量，默认值是 1，可选参数
ioThreadsCount=1
# 输出到日志服务的时间格式，使用 Java 中 SimpleDateFormat 格式化时间，默认是 ISO8601，可选参数
timeFormat=yyyy-MM-dd'T'HH:mmZ
timeZone=UTC
```