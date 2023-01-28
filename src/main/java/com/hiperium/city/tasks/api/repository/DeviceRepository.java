package com.hiperium.city.tasks.api.repository;

import com.hiperium.city.tasks.api.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Objects;

@Repository
public class DeviceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRepository.class);

    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private DynamoDbTable<Device> deviceTable;

    public DeviceRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.deviceTable = this.dynamoDbEnhancedClient.table(Device.TABLE_NAME, TableSchema.fromBean(Device.class));
    }

    public Device findById(String id) {
        LOGGER.debug("findById() - id: {}", id);
        return this.deviceTable.getItem(Device.builder().id(id).build());
    }

    public void update(Device updatedDevice) {
        LOGGER.debug("update() - device: {}", updatedDevice);
        Device actualDevice = this.deviceTable.getItem(Device.builder().id(updatedDevice.getId()).build());
        if (Objects.isNull(actualDevice)) {
            throw new IllegalArgumentException("Trying to update a Device that does not exist: " + updatedDevice.getId());
        }
        this.deviceTable.updateItem(updatedDevice);
    }
}
