package com.github.sebersole.gradle.quarkus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {
	// todo : use JBoss logging
	public static final String LOGGER_NAME = "com.github.sebersole.quarkus.gradle";
	public static final Logger LOGGER = LoggerFactory.getLogger( LOGGER_NAME );
	public static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();
	public static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();
}
