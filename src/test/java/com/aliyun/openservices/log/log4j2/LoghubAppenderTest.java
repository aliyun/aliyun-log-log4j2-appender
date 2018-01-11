package com.aliyun.openservices.log.log4j2;

import com.aliyun.openservices.log.producer.ProducerConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotEquals;

public class LoghubAppenderTest {

    private static final Logger LOGGER = LogManager.getLogger(LoghubAppenderTest.class);

    private static void sleep() {
        ProducerConfig producerConfig = new ProducerConfig();
        try {
            Thread.sleep(2 * producerConfig.packageTimeoutInMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void checkStatusDataList() {
        sleep();
        List<StatusData> statusDataList = StatusLogger.getLogger().getStatusData();
        for (StatusData statusData : statusDataList) {
            statusData.getLevel();
        }
        for (StatusData statusData : statusDataList) {
            Level level = statusData.getLevel();
            assertNotEquals(statusData.getMessage().toString(), Level.ERROR, level);
        }
    }

    @Test
    public void testLogCommonMessage() {
        LOGGER.warn("This is a test common message logged by log4j2.");
    }

    @Test
    public void testLogThrowable() {
        LOGGER.error("This is a test error message logged by log4j2.",
                new UnsupportedOperationException("Log4j2 UnsupportedOperationException"));
    }

    @Test
    public void testLogLevelInfo() {
        LOGGER.info("This is a test error message logged by log4j2, level is info,should not be logged.");
    }
}
