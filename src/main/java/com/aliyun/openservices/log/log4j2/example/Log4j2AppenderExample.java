package com.aliyun.openservices.log.log4j2.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by brucewu on 2018/1/8.
 */
public class Log4j2AppenderExample {

    private static final Logger LOGGER = LogManager.getLogger(Log4j2AppenderExample.class);

    public static void main(String[] args) throws InterruptedException {
        LOGGER.trace("log4j2 trace log");
        LOGGER.debug("log4j2 debug log");
        LOGGER.info("log4j2 info log");
        LOGGER.warn("log4j2 warn log");
        LOGGER.error("log4j2 error log", new RuntimeException("Runtime Exception"));
        Thread.sleep(5000);
    }

}
