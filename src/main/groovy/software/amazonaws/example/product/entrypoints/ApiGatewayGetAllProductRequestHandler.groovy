// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.fasterxml.jackson.core.JsonProcessingException
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.lambda.powertools.tracing.Tracing
import software.amazonaws.example.product.model.Products
import software.amazonaws.example.product.store.ProductStore
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore

import static groovy.json.JsonOutput.toJson
import static software.amazon.awssdk.http.HttpStatusCode.INTERNAL_SERVER_ERROR
import static software.amazon.awssdk.http.HttpStatusCode.OK

@CompileStatic
class ApiGatewayGetAllProductRequestHandler implements ApiGatewayRequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(ApiGatewayGetAllProductRequestHandler)
  private static final ProductStore productStore = new DynamoDbProductStore()

  @Override
  @Tracing
  APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    Products products
    try {
      products = productStore.getAllProduct()
    } catch (Exception e) {
      logger.error(e.getMessage(), e)
      return prepareResponse(INTERNAL_SERVER_ERROR, '{"message": "Failed to get products"}')
    }

    try {
      return prepareResponse(OK, toJson(products))
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage(), e)
      return prepareResponse(INTERNAL_SERVER_ERROR, '{"message": "Error Processing JSON response"}')
    }
  }
}
