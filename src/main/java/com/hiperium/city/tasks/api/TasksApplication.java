package com.hiperium.city.tasks.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiperium.city.tasks.api.utils.TasksUtil;
import com.hiperium.city.tasks.api.vo.AuroraPostgresSecretVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.MessageFormat;
import java.util.Objects;

@SpringBootApplication
public class TasksApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(TasksApplication.class);
	private static final String JDBC_SQL_CONNECTION = "jdbc:postgresql://{0}:{1}/{2}";

	public static void main(String[] args) throws JsonProcessingException {
		LOGGER.info("main() - BEGIN");
		settingJdbcConnection();
		settingTasksTimeZone();
		SpringApplication.run(TasksApplication.class, args);
		LOGGER.info("main() - END");
	}

	private static void settingJdbcConnection() throws JsonProcessingException {
		LOGGER.debug("settingJdbcConnection() - BEGIN");
		AuroraPostgresSecretVO auroraSecretVO = TasksUtil.getAuroraSecretVO();
		String sqlConnection = MessageFormat.format(JDBC_SQL_CONNECTION, auroraSecretVO.getHost(),
				auroraSecretVO.getPort(), auroraSecretVO.getDbname());
		LOGGER.debug("Setting JDBC Connection: {}", sqlConnection);
		System.setProperty("spring.jdbc.url", sqlConnection);
		System.setProperty("spring.jdbc.username", auroraSecretVO.getUsername());
		System.setProperty("spring.jdbc.password", auroraSecretVO.getPassword());
		LOGGER.debug("settingJdbcConnection() - END");
	}

	private static void settingTasksTimeZone() {
		LOGGER.debug("setTimeZoneProperties() - BEGIN");
		String timeZoneId = System.getenv("TIME_ZONE_ID");
		if (Objects.isNull(timeZoneId) || timeZoneId.isBlank()) {
			LOGGER.warn("TIME_ZONE_ID environment variable not found.");
			LOGGER.warn("Using the defined configuration property for Time Zone.");
		} else {
			LOGGER.debug("Time Zone ID from Environment Variable: {}", timeZoneId);
			System.setProperty("tasks.time.zone.id", timeZoneId);
		}
		LOGGER.debug("setTimeZoneProperties() - END");
	}
}
