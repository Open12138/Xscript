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
 * String variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class StringVariant extends Variant {

  // String value
  private String value;

  /**
   * Factory method for creating String variants.
   * 
   * @param value  String value
   * @return  new String variant
   */
  public static final StringVariant getStringVariant(String value) {
    return new StringVariant(value);
  }

  /*
   * Creates a new String variant.
   */
  private StringVariant(String value) {
    super(VARIANT_STRING);
    this.value = value;
  }

  @Override
  public boolean getBoolean() {
    return ConvHelpers.string2boolean(value);
  }

  @Override
  public byte getByte() {
    return ConvHelpers.string2byte(value);
  }

  @Override
  public short getShort() {
    return ConvHelpers.string2short(value);
  }

  @Override
  public int getInteger() {
    return ConvHelpers.string2integer(value);
  }

  @Override
  public long getLong() {
    return ConvHelpers.string2long(value);
  }

  @Override
  public float getSingle() {
    return ConvHelpers.string2single(value);
  }

  @Override
  public double getDouble() {
    return ConvHelpers.string2double(value);
  }

  @Override
  public String getString() {
    return value;
  }

  @Override
  public Variant add(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).add(rightOp);
  }

  @Override
  public Variant sub(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).sub(rightOp);
  }

  @Override
  public Variant mul(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).mul(rightOp);
  }

  @Override
  public Variant div(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).div(rightOp);
  }

  @Override
  public Variant idiv(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).idiv(rightOp);
  }

  @Override
  public Variant mod(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).mod(rightOp);
  }

  @Override
  public Variant pow(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).pow(rightOp);
  }

  @Override
  public Variant neg() {
    return DoubleVariant.getDoubleVariant(getDouble()).neg();
  }

  @Override
  public Variant shl(Variant rightOp) {
    return LongVariant.getLongVariant(getLong()).shl(rightOp);
  }

  @Override
  public Variant shr(Variant rightOp) {
    return LongVariant.getLongVariant(getLong()).shr(rightOp);
  }

  @Override
  public int cmp(Variant rightOp) {
    if (getKind() == VARIANT_STRING && rightOp.getKind() == VARIANT_STRING) {
      return value.compareTo(rightOp.getString());
    }

    return DoubleVariant.getDoubleVariant(getDouble()).cmp(rightOp);
  }

  @Override
  public boolean like(Variant rightOp) {
    return ExprHelpers.like(value, rightOp.getString());
  }

  @Override
  public Variant not() {
    return LongVariant.getLongVariant(getLong()).not();
  }

  @Override
  public Variant and(Variant rightOp) {
    return LongVariant.getLongVariant(getLong()).and(rightOp);
  }

  @Override
  public Variant or(Variant rightOp) {
    return LongVariant.getLongVariant(getLong()).or(rightOp);
  }

  @Override
  public Variant xor(Variant rightOp) {
    return LongVariant.getLongVariant(getLong()).xor(rightOp);
  }
}
