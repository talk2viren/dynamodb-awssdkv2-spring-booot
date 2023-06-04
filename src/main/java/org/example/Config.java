package org.example;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.core.waiters.WaiterResponse;

import java.net.URI;


public class Config {

    public static void main() {
        System.out.println("system check");
        ProfileCredentialsProvider credentialsProvider=ProfileCredentialsProvider.create();
        Region region= Region.US_EAST_1;
        DynamoDbClient dynamoDbClient = DynamoDbClient
                .builder()
                .endpointOverride(URI.create("http://localhost:8000"))
//                .credentialsProvider(credentialsProvider)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummy-key", "dummy-secret")))
                .region(region).build();

        System.out.println("Dynamodb Client : "+dynamoDbClient);
        createTable(dynamoDbClient,"test-emp-table","empId");

    }

    public static String createTable(DynamoDbClient ddb,String tableName,String key){
        DynamoDbWaiter dbWaiter= ddb.waiter();
        CreateTableRequest request=CreateTableRequest
                .builder()
                .attributeDefinitions(AttributeDefinition
                        .builder().attributeName(key)
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName(key)
                        .keyType(KeyType.HASH)
                        .build())
                .tableName(tableName)
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .build();

        String newTable = "";
        try{
            CreateTableResponse response = ddb.createTable(request);
            DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName).build();

            WaiterResponse<DescribeTableResponse> waiterResponse=dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            newTable = response.tableDescription().tableName();
            return newTable;
        }catch (DynamoDbException e){
            e.printStackTrace();
        }

        return "";
    }
}
