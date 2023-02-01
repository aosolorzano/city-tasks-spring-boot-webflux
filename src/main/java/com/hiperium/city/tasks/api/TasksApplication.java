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
		LOGGER.info("settingJdbcConnection() - BEGIN");
		AuroraPostgresSecretVO auroraSecretVO = TasksUtil.getAuroraSecretVO();
		if (Objects.nonNull(auroraSecretVO)) {
			String sqlConnection = MessageFormat.format(JDBC_SQL_CONNECTION, auroraSecretVO.getHost(),
					auroraSecretVO.getPort(), auroraSecretVO.getDbname());
			LOGGER.debug("Setting JDBC Connection: {}", sqlConnection);
			System.setProperty("spring.datasource.url", sqlConnection);
			System.setProperty("spring.datasource.username", auroraSecretVO.getUsername());
			System.setProperty("spring.datasource.password", auroraSecretVO.getPassword());
			System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
		}
		LOGGER.info("settingJdbcConnection() - END");
	}

	private static void settingTasksTimeZone() {
		LOGGER.info("settingTasksTimeZone() - BEGIN");
		String timeZoneId = TasksUtil.getTimeZoneId();
		if (Objects.nonNull(timeZoneId)) {
			LOGGER.debug("Time Zone ID from Environment Variable: {}", timeZoneId);
			System.setProperty("hiperium.city.tasks.time.zone.id", timeZoneId);
		}
		LOGGER.info("settingTasksTimeZone() - END");
	}
}
