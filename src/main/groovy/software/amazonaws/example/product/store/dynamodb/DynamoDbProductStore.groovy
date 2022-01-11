// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.SdkSystemSetting
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.lambda.powertools.tracing.Tracing
import software.amazonaws.example.product.model.Product
import software.amazonaws.example.product.model.Products
import software.amazonaws.example.product.store.ProductStore

@CompileStatic
class DynamoDbProductStore implements ProductStore {
  private static final Logger logger = LoggerFactory.getLogger(DynamoDbProductStore.class)
  private static final String PRODUCT_TABLE_NAME = System.getenv("PRODUCT_TABLE_NAME")
  private static final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
    .httpClientBuilder(AwsCrtAsyncHttpClient.builder().maxConcurrency(50))
    .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
    .build()

  static {
    try {
      dynamoDbClient.describeTable(
        DescribeTableRequest.builder().tableName(PRODUCT_TABLE_NAME).build() as DescribeTableRequest
      ).get()
    } catch (AwsServiceException | SdkClientException e) {
      logger.error "Error while creating DynamoDB connection during init phase: ${e.message}"
    }
  }

  @Override
  @Tracing
  Optional<Product> getProduct(String id) {
    GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
      .key(Map.of("PK", AttributeValue.builder().s(id).build()))
      .tableName(PRODUCT_TABLE_NAME)
      .build() as GetItemRequest)
    .get()

    if (getItemResponse.hasItem()) {
      return Optional.of(ProductMapper.productFromDynamoDB(getItemResponse.item()))
    } else {
      return Optional.empty()
    }
  }

  @Override
  @Tracing
  void putProduct(Product product) {
    dynamoDbClient.putItem(PutItemRequest.builder()
      .tableName(PRODUCT_TABLE_NAME)
      .item(ProductMapper.productToDynamoDb(product))
      .build() as PutItemRequest)
      .get()
  }

  @Override
  @Tracing
  void deleteProduct(String id) {
    dynamoDbClient.deleteItem(DeleteItemRequest.builder()
      .tableName(PRODUCT_TABLE_NAME)
      .key(Map.of("PK", AttributeValue.builder().s(id).build()))
      .build() as DeleteItemRequest)
      .get()
  }

  @Override
  @Tracing
  Products getAllProduct() {
    ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
      .tableName(PRODUCT_TABLE_NAME)
      .limit(20)
      .build() as ScanRequest)
      .get()

    logger.info "Scan returned: {} item(s)", scanResponse.count()

    List<Product> productList = scanResponse.items().collect {
      ProductMapper.productFromDynamoDB(it)
    }

    new Products(products: productList)
  }
}
