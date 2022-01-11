// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb

import groovy.transform.CompileStatic
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazonaws.example.product.model.Product

@CompileStatic
class ProductMapper {

  private static final String PK = "PK"
  private static final String NAME = "name"
  private static final String PRICE = "price"

  static Product productFromDynamoDB(Map<String, AttributeValue> items) {
    new Product(
      id: items[(PK)].s(),
      name: items[(NAME)].s(),
      price: items[(PRICE)].n() as BigDecimal
    )
  }

  static Map<String, AttributeValue> productToDynamoDb(Product product) {
    [
      (PK): AttributeValue.builder().s(product.getId()).build(),
      (NAME): AttributeValue.builder().s(product.getName()).build(),
      (PRICE): AttributeValue.builder().n(product.getPrice().toString()).build()
    ]
  }
}
