package com.hiperium.city.tasks.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiperium.city.tasks.api.vo.AuroraPostgresSecretVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class EnvironmentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentUtil.class);

    private EnvironmentUtil() {
        // Empty constructor.
    }

    public static AuroraPostgresSecretVO getAuroraSecretVO() throws JsonProcessingException {
        String auroraSecret = System.getenv("HIPERIUM_CITY_TASKS_DB_CLUSTER_SECRET");
        if (Objects.isNull(auroraSecret) || auroraSecret.isBlank()) {
            LOGGER.warn("HIPERIUM_CITY_TASKS_DB_CLUSTER_SECRET not found. Using defaults.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(auroraSecret, AuroraPostgresSecretVO.class);
    }

    public static String getTimeZoneId() {
        String timeZoneId = System.getenv("HIPERIUM_CITY_TASKS_TIME_ZONE_ID");
        if (Objects.isNull(timeZoneId) || timeZoneId.isBlank()) {
            LOGGER.warn("HIPERIUM_CITY_TASKS_TIME_ZONE_ID not found. Using defaults.");
            return null;
        }
        return timeZoneId;
    }

    public static String getAwsEndpointOverride() {
        String endpointOverride = System.getenv("AWS_DYNAMODB_ENDPOINT-OVERRIDE");
        if (Objects.isNull(endpointOverride) || endpointOverride.isBlank()) {
            LOGGER.warn("AWS_DYNAMODB_ENDPOINT-OVERRIDE not found. Using defaults.");
            return null;
        }
        return endpointOverride;
    }

    public static String getAwsRegion() {
        String awsRegion = System.getenv("AWS_DEFAULT_REGION");
        if (Objects.isNull(awsRegion) || awsRegion.isBlank()) {
            LOGGER.warn("AWS_DEFAULT_REGION not found. Using defaults.");
            return null;
        }
        return awsRegion;
    }
}
