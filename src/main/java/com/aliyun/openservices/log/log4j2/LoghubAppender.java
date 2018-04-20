package com.aliyun.openservices.log.log4j2;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.producer.LogProducer;
import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Throwables;

@Plugin(name = "Loghub", category = "Core", elementType = "appender", printObject = true)
public class LoghubAppender extends AbstractAppender {

    protected String projectName;
    protected String logstore;
    protected String endpoint;
    protected String accessKeyId;
    protected String accessKey;
    protected String stsToken;
    protected int packageTimeoutInMS;
    protected int logsCountPerPackage;
    protected int logsBytesPerPackage;
    protected int memPoolSizeInByte;
    protected int retryTimes;
    protected int maxIOThreadSizeInPool;
    protected final DateFormat dateFormat;

    private LogProducer producer;
    private String topic = "";

    private static final String DEFAULT_TIME_FORMAT_STR = "yyyy-MM-dd'T'HH:mmZ";

    private static final String DEFAULT_TIME_ZONE = "UTC";

    private static final DateFormat DEFAULT_TIME_FORMAT;

    static {
        DEFAULT_TIME_FORMAT = new SimpleDateFormat(DEFAULT_TIME_FORMAT_STR);
        DEFAULT_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
    }

    protected LoghubAppender(String name,
                             Filter filter,
                             Layout<? extends Serializable> layout,
                             boolean ignoreExceptions,
                             String projectName,
                             String logstore,
                             String endpoint,
                             String accessKeyId,
                             String accessKey,
                             String stsToken,
                             int packageTimeoutInMS,
                             int logsCountPerPackage,
                             int logsBytesPerPackage,
                             int memPoolSizeInByte,
                             int retryTimes,
                             int maxIOThreadSizeInPool,
                             String topic,
                             DateFormat dateFormat
    ) {
        super(name, filter, layout, ignoreExceptions);
        this.projectName = projectName;
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.accessKeyId = accessKeyId;
        this.stsToken = stsToken;
        this.logstore = logstore;
        this.packageTimeoutInMS = packageTimeoutInMS;
        this.logsCountPerPackage = logsCountPerPackage;
        this.logsBytesPerPackage = logsBytesPerPackage;
        this.memPoolSizeInByte = memPoolSizeInByte;
        this.retryTimes = retryTimes;
        this.maxIOThreadSizeInPool = maxIOThreadSizeInPool;
        this.topic = topic;
        this.dateFormat = dateFormat;
    }

    @Override
    public void start() {
        super.start();

        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.projectName = this.projectName;
        projectConfig.accessKey = this.accessKey;
        projectConfig.accessKeyId = this.accessKeyId;
        projectConfig.endpoint = this.endpoint;
        projectConfig.stsToken = this.stsToken;

        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.packageTimeoutInMS = this.packageTimeoutInMS;
        producerConfig.logsCountPerPackage = this.logsCountPerPackage;
        producerConfig.logsBytesPerPackage = this.logsBytesPerPackage;
        producerConfig.memPoolSizeInByte = this.memPoolSizeInByte;
        producerConfig.retryTimes = this.retryTimes;
        producerConfig.maxIOThreadSizeInPool = this.maxIOThreadSizeInPool;
        producerConfig.userAgent = "log4j2";

        producer = new LogProducer(producerConfig);
        producer.setProjectConfig(projectConfig);
    }

    @Override
    public void stop() {
        super.stop();
        if (producer != null) {
            producer.flush();
            producer.close();
        }

    }

    @Override
    public void append(LogEvent event) {
        List<LogItem> logItems = new ArrayList<LogItem>();
        LogItem item = new LogItem();
        logItems.add(item);
        item.SetTime((int) (event.getTimeMillis() / 1000));
        item.PushBack("time", this.dateFormat.format(new Date(event.getTimeMillis())));
        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());

        StackTraceElement source = event.getSource();
        if (source == null && (!event.isIncludeLocation())) {
            event.setIncludeLocation(true);
            source = event.getSource();
            event.setIncludeLocation(false);
        }

        item.PushBack("location", source == null ? "Unknown(Unknown Source)" : source.toString());

        String message = event.getMessage().getFormattedMessage();
        Throwable throwable = event.getThrown();
        if (throwable != null) {
            for (String s : Throwables.toStringList(throwable)) {
                message += System.getProperty("line.separator") + s;
            }
        }

        item.PushBack("message", message);
        Map<String, String> properties = event.getContextMap();
        if (properties.size() > 0) {
            Object[] keys = properties.keySet().toArray();
            Arrays.sort(keys);
            for (int i = 0; i < keys.length; i++) {
                item.PushBack(keys[i].toString(), properties.get(keys[i].toString()));
            }
        }
        producer.send(this.projectName, this.logstore, this.topic, null, logItems, new LoghubAppenderCallback(LOGGER,
                this.projectName, this.logstore, this.topic, null, logItems));
    }

    @PluginFactory
    public static LoghubAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginConfiguration final Configuration config,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("projectName") final String projectName,
            @PluginAttribute("logstore") final String logstore,
            @PluginAttribute("endpoint") final String endpoint,
            @PluginAttribute("accessKeyId") final String accessKeyId,
            @PluginAttribute("accessKey") final String accessKey,
            @PluginAttribute("stsToken") final String stsToken,
            @PluginAttribute("packageTimeoutInMS") final String packageTimeoutInMS, // int
            @PluginAttribute("logsCountPerPackage") final String logsCountPerPackage, // int
            @PluginAttribute("logsBytesPerPackage") final String logsBytesPerPackage, // int
            @PluginAttribute("memPoolSizeInByte") final String memPoolSizeInByte, // int
            @PluginAttribute("retryTimes") final String retryTimes, //int
            @PluginAttribute("maxIOThreadSizeInPool") final String maxIOThreadSizeInPool, //int
            @PluginAttribute("topic") final String topic,
            @PluginAttribute("timeFormat") final String timeFormat,
            @PluginAttribute("timeZone") final String timeZone) {

        Boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);

        checkCondition(!isStrEmpty(projectName), "Config value [projectName] must not be null.");
        checkCondition(!isStrEmpty(logstore), "Config value [logstore] must not be null.");
        checkCondition(!isStrEmpty(endpoint), "Config value [endpoint] must not be null.");
        checkCondition(!isStrEmpty(accessKeyId), "Config value [accessKeyId] must not be null.");
        checkCondition(!isStrEmpty(accessKey), "Config value [accessKey] must not be null.");

        int packageTimeoutInMSInt = parseStrToInt(packageTimeoutInMS, 3000);
        checkCondition((packageTimeoutInMSInt > 10), "Config value [packageTimeoutInMS] must >10.");

        int logsCountPerPackageInt = parseStrToInt(logsCountPerPackage, 4096);
        checkCondition((logsCountPerPackageInt >= 1 && logsCountPerPackageInt <= 4096),
                "Config value [logsCountPerPackage] must between [1,4096].");

        int logsBytesPerPackageInt = parseStrToInt(logsBytesPerPackage, 5 * 1024 * 1024);
        checkCondition((logsBytesPerPackageInt >= 1 && logsBytesPerPackageInt <= 5 * 1024 * 1024),
                "Config value [logsBytesPerPackage] must between [1,3145728].");

        int memPoolSizeInByteInt = parseStrToInt(memPoolSizeInByte, 104857600);
        checkCondition((memPoolSizeInByteInt > 0), "Config value [memPoolSizeInByte] must > 0.");

        int retryTimesInt = parseStrToInt(retryTimes, 3);
        checkCondition((retryTimesInt > 0), "Config value [retryTimes] must > 0.");

        int maxIOThreadSizeInPoolInt = parseStrToInt(maxIOThreadSizeInPool, 8);
        checkCondition((maxIOThreadSizeInPoolInt > 0), "Config value [maxIOThreadSizeInPool] must > 0.");

        DateFormat tmpDateFormat;
        try {
            tmpDateFormat = new SimpleDateFormat(isStrEmpty(timeFormat) ? DEFAULT_TIME_FORMAT_STR : timeFormat);
            tmpDateFormat.setTimeZone(TimeZone.getTimeZone(isStrEmpty(timeZone) ? DEFAULT_TIME_ZONE : timeZone));
        } catch (Exception e) {
            tmpDateFormat = DEFAULT_TIME_FORMAT;
        }

        return new LoghubAppender(name, filter, layout, ignoreExceptions, projectName, logstore, endpoint,
                accessKeyId, accessKey, stsToken, packageTimeoutInMSInt, logsCountPerPackageInt, logsBytesPerPackageInt,
                memPoolSizeInByteInt, retryTimesInt, maxIOThreadSizeInPoolInt, topic, tmpDateFormat);
    }

    static boolean isStrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static void threadSleepInMills(long timeInMills) {
        try {
            Thread.sleep(timeInMills);
        } catch (Throwable e) {
            //ignore
        }
    }

    static int parseStrToInt(String str, final int defaultVal) {
        if (!isStrEmpty(str)) {
            try {
                return Integer.valueOf(str);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        } else {
            return defaultVal;
        }
    }

    static void checkCondition(Boolean condition, String errorMsg) {
        if (!condition) {
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
