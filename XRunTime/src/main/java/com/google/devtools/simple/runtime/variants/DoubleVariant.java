/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.runtime.variants;

import com.google.devtools.simple.runtime.helpers.ConvHelpers;
import com.google.devtools.simple.runtime.helpers.ExprHelpers;

/**
 * Double variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class DoubleVariant extends Variant {

  // Double value
  private double value;

  /**
   * Factory method for creating Double variants.
   * 
   * @param value  Double value
   * @return  new Double variant
   */
  public static final DoubleVariant getDoubleVariant(double value) {
    return new DoubleVariant(value);
  }

  /*
   * Creates a new Double variant.
   */
  private DoubleVariant(double value) {
    super(VARIANT_DOUBLE);
    this.value = value;
  }

  @Override
  public boolean getBoolean() {
    return ConvHelpers.double2boolean(value);
  }

  @Override
  public byte getByte() {
    return ConvHelpers.double2byte(value);
  }

  @Override
  public short getShort() {
    return ConvHelpers.double2short(value);
  }

  @Override
  public int getInteger() {
    return (int) value;
  }

  @Override
  public long getLong() {
    return (long) value;
  }

  @Override
  public float getSingle() {
    return (float) value;
  }

  @Override
  public double getDouble() {
    return value;
  }

  @Override
  public String getString() {
    return Double.toString(value);
  }

  @Override
  public Variant add(Variant rightOp) {
    return getDoubleVariant(value + rightOp.getDouble());
  }

  @Override
  public Variant sub(Variant rightOp) {
    return getDoubleVariant(value - rightOp.getDouble());
  }

  @Override
  public Variant mul(Variant rightOp) {
    return getDoubleVariant(value * rightOp.getDouble());
  }

  @Override
  public Variant div(Variant rightOp) {
    return getDoubleVariant(value / rightOp.getDouble());
  }

  @Override
  public Variant idiv(Variant rightOp) {
    return getDoubleVariant(ExprHelpers.idiv(value, rightOp.getDouble()));
  }

  @Override
  public Variant mod(Variant rightOp) {
    return getDoubleVariant(value % rightOp.getDouble());
  }

  @Override
  public Variant pow(Variant rightOp) {
    return getDoubleVariant(Math.pow(value, rightOp.getDouble()));
  }

  @Override
  public Variant neg() {
    return getDoubleVariant(-value);
  }

  @Override
  public int cmp(Variant rightOp) {
    double rightValue = rightOp.getDouble();
    if (value == rightValue) {
      return 0;
    }
    return value > rightValue ? 1 : -1;
  }
}
