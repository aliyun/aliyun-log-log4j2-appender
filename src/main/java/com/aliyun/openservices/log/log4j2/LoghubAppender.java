package com.aliyun.openservices.log.log4j2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.log.common.LogItem;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Plugin(name = "Loghub", category = "Core", elementType = "appender", printObject = true)
public class LoghubAppender extends AbstractAppender {

    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static final String DEFAULT_TIME_ZONE = "UTC";

    protected String project;
    protected String logStore;
    protected String endpoint;
    protected String accessKeyId;
    protected String accessKeySecret;
    protected String stsToken;

    protected int totalSizeInBytes;
    protected int maxBlockMs;
    protected int ioThreadCount;
    protected int batchSizeThresholdInBytes;
    protected int batchCountThreshold;
    protected int lingerMs;
    protected int retries;
    protected int baseRetryBackoffMs;
    protected int maxRetryBackoffMs;

    private String userAgent = "log4j2";
    private Producer producer;
    private String topic;
    private String source;
    private ProducerConfig producerConfig = new ProducerConfig();

    private DateTimeFormatter formatter;
    private String mdcFields;

    protected LoghubAppender(String name,
                             Filter filter,
                             Layout<? extends Serializable> layout,
                             boolean ignoreExceptions,
                             String project,
                             String logStore,
                             String endpoint,
                             String accessKeyId,
                             String accessKeySecret,
                             String stsToken,
                             int totalSizeInBytes,
                             int maxBlockMs,
                             int ioThreadCount,
                             int batchSizeThresholdInBytes,
                             int batchCountThreshold,
                             int lingerMs,
                             int retries,
                             int baseRetryBackoffMs,
                             int maxRetryBackoffMs,
                             String topic,
                             String source,
                             DateTimeFormatter formatter,
                             String mdcFields
    ) {
        super(name, filter, layout, ignoreExceptions);
        this.project = project;
        this.endpoint = endpoint;
        this.accessKeySecret = accessKeySecret;
        this.accessKeyId = accessKeyId;
        this.stsToken = stsToken;
        this.logStore = logStore;

        this.totalSizeInBytes = totalSizeInBytes;
        this.retries = retries;
        this.ioThreadCount = ioThreadCount;
        this.maxBlockMs = maxBlockMs;
        this.batchCountThreshold = batchCountThreshold;
        this.batchSizeThresholdInBytes = batchSizeThresholdInBytes;
        this.lingerMs = lingerMs;
        this.baseRetryBackoffMs = baseRetryBackoffMs;
        this.maxRetryBackoffMs = maxRetryBackoffMs;
        if (topic == null) {
            this.topic = "";
        } else {
            this.topic = topic;
        }
        this.source = source;
        this.formatter = formatter;
        this.mdcFields = mdcFields;
    }

    @Override
    public void start() {
        super.start();

        ProjectConfig projectConfig = buildProjectConfig();

        producerConfig.setBatchCountThreshold(batchCountThreshold);
        producerConfig.setBatchSizeThresholdInBytes(batchSizeThresholdInBytes);
        producerConfig.setIoThreadCount(ioThreadCount);
        producerConfig.setRetries(retries);
        producerConfig.setBaseRetryBackoffMs(baseRetryBackoffMs);
        producerConfig.setLingerMs(lingerMs);
        producerConfig.setMaxBlockMs(maxBlockMs);
        producerConfig.setMaxRetryBackoffMs(maxRetryBackoffMs);

        producer = new LogProducer(producerConfig);
        producer.putProjectConfig(projectConfig);
    }

    private ProjectConfig buildProjectConfig() {
        return new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret, null, userAgent);
    }

    @Override
    public void stop() {
        super.stop();
        if (producer != null) {
            try {
                producer.close();
            } catch (Exception e) {
                this.error("Failed to close LoghubAppender.", e);
            }
        }

    }

    @Override
    public void append(LogEvent event) {
        List<LogItem> logItems = new ArrayList<LogItem>();
        LogItem item = new LogItem();
        logItems.add(item);
        item.SetTime((int) (event.getTimeMillis() / 1000));
        DateTime dateTime = new DateTime(event.getTimeMillis());
        item.PushBack("time", dateTime.toString(formatter));
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
        item.PushBack("message", message);

        String throwable = getThrowableStr(event.getThrown());
        if (throwable != null) {
            item.PushBack("throwable", throwable);
        }

        if (getLayout() != null) {
            item.PushBack("log", new String(getLayout().toByteArray(event)));
        }

        Optional.ofNullable(mdcFields).ifPresent(
                f->event.getContextMap().entrySet().stream()
                        .filter(v->Arrays.stream(f.split(",")).anyMatch(i->i.equals(v.getKey())))
                        .forEach(map-> item.PushBack(map.getKey(),map.getValue()))
        );
        try {
            producer.send(this.project, this.logStore, this.topic, this.source, logItems, new LoghubAppenderCallback(LOGGER,
                    this.project, this.logStore, this.topic, this.source, logItems));
        } catch (Exception e) {
            this.error(
                    "Failed to send log, project=" + project
                            + ", logStore=" + logStore
                            + ", topic=" + topic
                            + ", source=" + source
                            + ", logItem=" + logItems, e);
        }
    }

    private String getThrowableStr(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : Throwables.toStringList(throwable)) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
        }
        return sb.toString();
    }

    @PluginFactory
    public static LoghubAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginConfiguration final Configuration config,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("project") final String project,
            @PluginAttribute("logStore") final String logStore,
            @PluginAttribute("endpoint") final String endpoint,
            @PluginAttribute("accessKeyId") final String accessKeyId,
            @PluginAttribute("accessKeySecret") final String accessKeySecret,
            @PluginAttribute("stsToken") final String stsToken,

            @PluginAttribute("totalSizeInBytes") final String  totalSizeInBytes,
            @PluginAttribute("maxBlockMs") final String  maxBlockMs,
            @PluginAttribute("ioThreadCount") final String  ioThreadCount,
            @PluginAttribute("batchSizeThresholdInBytes") final String  batchSizeThresholdInBytes,
            @PluginAttribute("batchCountThreshold") final String  batchCountThreshold,
            @PluginAttribute("lingerMs") final String  lingerMs,
            @PluginAttribute("retries") final String  retries,
            @PluginAttribute("baseRetryBackoffMs") final String  baseRetryBackoffMs,
            @PluginAttribute("maxRetryBackoffMs") final String maxRetryBackoffMs,

            @PluginAttribute("topic") final String topic,
            @PluginAttribute("source") final String source,
            @PluginAttribute("timeFormat") final String timeFormat,
            @PluginAttribute("timeZone") final String timeZone,
            @PluginAttribute("mdcFields") final String mdcFields) {

        Boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);

        int maxBlockMsInt = parseStrToInt(maxBlockMs, 0);

        int baseRetryBackoffMsInt = parseStrToInt(baseRetryBackoffMs, 100);

        int maxRetryBackoffMsInt = parseStrToInt(maxRetryBackoffMs, 100);

        int lingerMsInt = parseStrToInt(lingerMs, 3000);

        int batchCountThresholdInt = parseStrToInt(batchCountThreshold, 4096);

        int batchSizeThresholdInBytesInt = parseStrToInt(batchSizeThresholdInBytes, 5 * 1024 * 1024);

        int totalSizeInBytesInt = parseStrToInt(totalSizeInBytes, 104857600);

        int retriesInt = parseStrToInt(retries, 3);

        int ioThreadCountInt = parseStrToInt(ioThreadCount, 8);

        String pattern = isStrEmpty(timeFormat) ? DEFAULT_TIME_FORMAT : timeFormat;
        String timeZoneInfo = isStrEmpty(timeZone) ? DEFAULT_TIME_ZONE : timeZone;
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID(timeZoneInfo));

        return new LoghubAppender(name, filter, layout, ignoreExceptions, project, logStore, endpoint,
                accessKeyId, accessKeySecret, stsToken,totalSizeInBytesInt,maxBlockMsInt,ioThreadCountInt,
                batchSizeThresholdInBytesInt,batchCountThresholdInt,lingerMsInt,retriesInt,
                baseRetryBackoffMsInt, maxRetryBackoffMsInt,topic, source, formatter,mdcFields);
    }

    static boolean isStrEmpty(String str) {
        return str == null || str.length() == 0;
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

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getLogStore() {
        return logStore;
    }

    public void setLogStore(String logStore) {
        this.logStore = logStore;
    }

    public int getTotalSizeInBytes() {
        return producerConfig.getTotalSizeInBytes();
    }

    public void setTotalSizeInBytes(int totalSizeInBytes) {
        producerConfig.setTotalSizeInBytes(totalSizeInBytes);
    }

    public long getMaxBlockMs() {
        return producerConfig.getMaxBlockMs();
    }

    public void setMaxBlockMs(long maxBlockMs) {
        producerConfig.setMaxBlockMs(maxBlockMs);
    }

    public int getIoThreadCount() {
        return producerConfig.getIoThreadCount();
    }

    public void setIoThreadCount(int ioThreadCount) {
        producerConfig.setIoThreadCount(ioThreadCount);
    }

    public int getBatchSizeThresholdInBytes() {
        return producerConfig.getBatchSizeThresholdInBytes();
    }

    public void setBatchSizeThresholdInBytes(int batchSizeThresholdInBytes) {
        producerConfig.setBatchSizeThresholdInBytes(batchSizeThresholdInBytes);
    }

    public int getBatchCountThreshold() {
        return producerConfig.getBatchCountThreshold();
    }

    public void setBatchCountThreshold(int batchCountThreshold) {
        producerConfig.setBatchCountThreshold(batchCountThreshold);
    }

    public int getLingerMs() {
        return producerConfig.getLingerMs();
    }

    public void setLingerMs(int lingerMs) {
        producerConfig.setLingerMs(lingerMs);
    }

    public int getRetries() {
        return producerConfig.getRetries();
    }

    public void setRetries(int retries) {
        producerConfig.setRetries(retries);
    }

    public int getMaxReservedAttempts() {
        return producerConfig.getMaxReservedAttempts();
    }

    public void setMaxReservedAttempts(int maxReservedAttempts) {
        producerConfig.setMaxReservedAttempts(maxReservedAttempts);
    }

    public long getBaseRetryBackoffMs() {
        return producerConfig.getBaseRetryBackoffMs();
    }

    public void setBaseRetryBackoffMs(long baseRetryBackoffMs) {
        producerConfig.setBaseRetryBackoffMs(baseRetryBackoffMs);
    }

    public long getMaxRetryBackoffMs() {
        return producerConfig.getMaxRetryBackoffMs();
    }

    public void setMaxRetryBackoffMs(long maxRetryBackoffMs) {
        producerConfig.setMaxRetryBackoffMs(maxRetryBackoffMs);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMdcFields(String mdcFields) {
        this.mdcFields = mdcFields;
    }
}
