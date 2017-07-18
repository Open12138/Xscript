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
 * Single variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class SingleVariant extends Variant {

  // Single value
  private float value;

  /**
   * Factory method for creating Single variants.
   * 
   * @param value  Single value
   * @return  new Single variant
   */
  public static final SingleVariant getSingleVariant(float value) {
    return new SingleVariant(value);
  }

  /*
   * Creates a new Single variant.
   */
  private SingleVariant(float value) {
    super(VARIANT_SINGLE);
    this.value = value;
  }

  @Override
  public boolean getBoolean() {
    return ConvHelpers.single2boolean(value);
  }

  @Override
  public byte getByte() {
    return ConvHelpers.single2byte(value);
  }

  @Override
  public short getShort() {
    return ConvHelpers.single2short(value);
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
    return value;
  }

  @Override
  public double getDouble() {
    return value;
  }

  @Override
  public String getString() {
    return Float.toString(value);
  }

  @Override
  public Variant add(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        return rightOp.add(this);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(getSingle() + rightOp.getSingle());
    }
  }

  @Override
  public Variant sub(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.sub(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(getSingle() - rightOp.getSingle());
  
      case VARIANT_DOUBLE:
      case VARIANT_STRING:
        return DoubleVariant.getDoubleVariant(getDouble() - rightOp.getDouble());
    }
  }

  @Override
  public Variant mul(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        return rightOp.mul(this);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(getSingle() * rightOp.getSingle());
    }
  }

  @Override
  public Variant div(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).div(rightOp);
  }

  @Override
  public Variant idiv(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.idiv(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(ExprHelpers.idiv(getSingle(), rightOp.getSingle()));
  
      case VARIANT_DOUBLE:
      case VARIANT_STRING:
        return DoubleVariant.getDoubleVariant(ExprHelpers.idiv(getDouble(), rightOp.getDouble()));
    }
  }

  @Override
  public Variant mod(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.mod(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(getSingle() % rightOp.getSingle());
  
      case VARIANT_DOUBLE:
      case VARIANT_STRING:
        return DoubleVariant.getDoubleVariant(getDouble() % rightOp.getDouble());
    }
  }

  @Override
  public Variant pow(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(Math.pow(getDouble(), rightOp.getDouble()));
  }

  @Override
  public Variant neg() {
    return SingleVariant.getSingleVariant(-value);
  }

  @Override
  public int cmp(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        return -rightOp.cmp(this);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_SINGLE:
        float rvalue = rightOp.getSingle();
        if (value == rvalue) {
          return 0;
        }
        return value > rvalue ? 1 : -1;
    }
  }
}
