// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.lambda.powertools.tracing.Tracing
import software.amazonaws.example.product.model.Product
import software.amazonaws.example.product.store.ProductStore
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore

import static software.amazon.awssdk.http.HttpStatusCode.*

@CompileStatic
class ApiGatewayPutProductRequestHandler implements ApiGatewayRequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPutProductRequestHandler.class)
  private static final ObjectMapper objectMapper = new ObjectMapper()
  private static final ProductStore productStore = new DynamoDbProductStore()

  @Override
  @Tracing
  APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    logger.info("Event body: " + event.getBody())

    String id = event.pathParameters["id"]

    if (!id) {
      logger.warn("Missing 'id' parameter in path")
      return prepareResponse(BAD_REQUEST, /{ "message": "Missing 'id' parameter in path" }/)
    }

    if (!event.body) {
      return prepareResponse(BAD_REQUEST, '{"message": "Empty request body"}')
    }

    Product product = null
    try {
      product = objectMapper.readValue(event.body, Product)
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage())
      return prepareResponse(BAD_REQUEST, '{"message": "Failed to parse product from request body"}')
    }

    if (id != product.id) {
      logger.error("Product ID in path ({}) does not match product ID in body ({})", id, product.getId())
      return prepareResponse(BAD_REQUEST, /{"message": "Product ID in path does not match product ID in body"}/)
    }

    logger.info("Parsed: " + product)

    try {
      productStore.putProduct(product)
    } catch (Exception e) {
      logger.error(e.getMessage())
      return prepareResponse(INTERNAL_SERVER_ERROR, /{"message": "Exception adding Product"}/)
    }

    prepareResponse(CREATED, '{"message": "Product created"}')
  }
}
