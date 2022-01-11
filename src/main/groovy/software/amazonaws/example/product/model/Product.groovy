// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.model

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.math.RoundingMode

@CompileStatic
@ToString
class Product {
  String id
  String name
  BigDecimal price

  void setPrice(BigDecimal price) {
    this.price = price.setScale(2, RoundingMode.HALF_UP)
  }
}
