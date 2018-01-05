package com.aliyun.openservices.log.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class LoghubAppenderTest {

    private static Logger logger = LogManager.getLogger("slsLogger");

    @Test
    public void testLogCommonMessage() {
        logger.warn("This is a test common message logged by log4j2.");
    }

    @Test
    public void testLogThrowable() {
        logger.error("This is a test error message logged by log4j2.",
                new UnsupportedOperationException("log4j2 appender test."));
    }

    @Test
    public void testLogLevelInfo() {
        logger.info("This is a test error message logged by log4j2, level is info,should not be logged.");
    }
}
