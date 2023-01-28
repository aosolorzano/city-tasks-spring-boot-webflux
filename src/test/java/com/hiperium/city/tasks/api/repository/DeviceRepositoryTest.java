package com.hiperium.city.tasks.api.repository;

import com.hiperium.city.tasks.api.common.AbstractContainerBase;
import com.hiperium.city.tasks.api.model.Device;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.net.URI;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeviceRepositoryTest extends AbstractContainerBase {

    public static final String DEVICE_ID = "1";

    private static DynamoDbClient ddb;

    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeAll
    public static void init() {
        ddb = DynamoDbClient.builder()
                .endpointOverride(URI.create(LOCAL_STACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())
                        )
                )
                .region(Region.of(LOCAL_STACK_CONTAINER.getRegion()))
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Create Devices Table")
    void givenDynamoDBClient_whenCreateTable_mustCreateTable() {
        DynamoDbWaiter dbWaiter = ddb.waiter();
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .tableName(Device.TABLE_NAME)
                .build();
        try {
            CreateTableResponse response = ddb.createTable(request);
            DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                    .tableName(Device.TABLE_NAME)
                    .build();
            WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            String newTable = response.tableDescription().tableName();
            Assertions.assertThat(newTable).isEqualTo(Device.TABLE_NAME);
        } catch (DynamoDbException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Create Device Item")
    void givenDeviceObject_whenSave_mustSaveDeviceItem() {
        try {
            DynamoDbTable<Device> deviceTable = this.dynamoDbEnhancedClient
                    .table(Device.TABLE_NAME, TableSchema.fromBean(Device.class));
            Device device = getNewDevice();
            deviceTable.putItem(device);
        } catch (DynamoDbException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Find Device by ID")
    void givenDeviceId_whenFindById_mustReturnDevice() {
        Device device = this.deviceRepository.findById("1");
        Assertions.assertThat(device).isNotNull();
        Assertions.assertThat(device.getId()).isEqualTo(DEVICE_ID);
        Assertions.assertThat(device.getName()).isEqualTo("Device 1");
    }

    @Test
    @Order(4)
    @DisplayName("Find Device with wrong ID")
    void givenDeviceId_whenFindById_mustReturnNull() {
        Device device = this.deviceRepository.findById("100");
        Assertions.assertThat(device).isNull();
    }

    @Test
    @Order(5)
    @DisplayName("Update Device Item")
    void givenDeviceItem_whenUpdate_mustUpdateDeviceItem() {
        Device device = this.deviceRepository.findById(DEVICE_ID);
        device.setName("Device 1 Updated");
        this.deviceRepository.update(device);
        Device deviceUpdated = this.deviceRepository.findById(DEVICE_ID);
        Assertions.assertThat(deviceUpdated).isNotNull();
        Assertions.assertThat(deviceUpdated.getId()).isEqualTo(DEVICE_ID);
        Assertions.assertThat(deviceUpdated.getName()).isEqualTo("Device 1 Updated");
    }

    @Test
    @Order(6)
    @DisplayName("Update not existing Device Item")
    void givenDeviceItem_whenUpdate_mustThrowException() {
        Device device = getNewDevice();
        device.setId("100");
        Assertions.assertThatThrownBy(() -> this.deviceRepository.update(device))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    private static Device getNewDevice() {
        Device device = Device.builder()
                .id(DEVICE_ID)
                .name("Device 1")
                .build();
        return device;
    }
}
