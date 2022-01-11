// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints

import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import groovy.transform.CompileStatic

import static software.amazon.awssdk.http.Header.CONTENT_TYPE

@CompileStatic
trait ApiGatewayRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  APIGatewayV2HTTPResponse prepareResponse(final int statusCode, final String body) {
    new APIGatewayV2HTTPResponse(
      statusCode: statusCode,
      headers: [(CONTENT_TYPE): 'application/json'],
      body: body
    )
  }
}