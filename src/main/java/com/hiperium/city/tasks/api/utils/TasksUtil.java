package com.hiperium.city.tasks.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiperium.city.tasks.api.exception.TaskScheduleException;
import com.hiperium.city.tasks.api.model.Task;
import com.hiperium.city.tasks.api.vo.AuroraPostgresSecretVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

public final class TasksUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksUtil.class);

    public static final String TASKS_PATH = "/api/tasks";
    private static final char[] HEX_ARRAY = "HiperiumTasksService".toCharArray();
    private static final int JOB_ID_LENGTH = 20;

    private TasksUtil() {
        // Empty constructor.
    }

    public static AuroraPostgresSecretVO getAuroraSecretVO() throws JsonProcessingException {
        String auroraSecret = System.getenv("HIPERIUM_CITY_TASKS_DB_CLUSTER_SECRET");
        if (Objects.isNull(auroraSecret) || auroraSecret.isBlank()) {
            LOGGER.warn("HIPERIUM_CITY_TASKS_DB_CLUSTER_SECRET environment variable not found.");
            LOGGER.warn("Using the default Data Source configuration.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(auroraSecret, AuroraPostgresSecretVO.class);
    }

    public static String getTimeZoneId() {
        String timeZoneId = System.getenv("HIPERIUM_CITY_TASKS_TIME_ZONE");
        if (Objects.isNull(timeZoneId) || timeZoneId.isBlank()) {
            LOGGER.warn("HIPERIUM_CITY_TASKS_TIME_ZONE environment variable not found.");
            LOGGER.warn("Using the default configuration for the Time Zone.");
            return null;
        }
        return timeZoneId;
    }

    public static String generateJobId() {
        MessageDigest salt;
        try {
            salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
        String uuid = bytesToHex(salt.digest());
        return uuid.substring(0, JOB_ID_LENGTH);
    }

    public static Task validateTaskId(Task newTask) {
        if (Objects.nonNull(newTask.getId())) {
            throw new TaskScheduleException("Task already exists with ID: " + newTask.getId() + ".");
        }
        return newTask;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
