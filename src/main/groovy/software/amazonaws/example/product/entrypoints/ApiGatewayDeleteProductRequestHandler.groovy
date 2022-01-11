// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.lambda.powertools.tracing.Tracing
import software.amazonaws.example.product.store.ProductStore
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore

import static software.amazon.awssdk.http.HttpStatusCode.*

@CompileStatic
class ApiGatewayDeleteProductRequestHandler implements ApiGatewayRequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(ApiGatewayDeleteProductRequestHandler.class)
  private static final ProductStore productStore = new DynamoDbProductStore()

  @Override
  @Tracing
  APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    String id = event.pathParameters["id"]

    if (!id) {
      logger.warn "Missing 'id' parameter in path"
      return prepareResponse(BAD_REQUEST, /{ "message": "Missing 'id' parameter in path" }/)
    }

    try {
      productStore.deleteProduct(id)
    } catch (Exception e) {
      logger.error e.getMessage()
      return prepareResponse(INTERNAL_SERVER_ERROR, /{"message": "Failed to delete product"}/)
    }

    prepareResponse(OK, '{"message": "Product deleted"}')
  }
}
