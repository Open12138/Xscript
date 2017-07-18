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

/**
 * Boolean variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class BooleanVariant extends Variant {

  // Boolean value
  private boolean value;

  /**
   * Factory method for creating Boolean variants.
   * 
   * @param value  Boolean value
   * @return  new Boolean variant
   */
  public static final BooleanVariant getBooleanVariant(boolean value) {
    return new BooleanVariant(value);
  }

  /*
   * Creates a new Boolean variant.
   */
  private BooleanVariant(boolean value) {
    super(VARIANT_BOOLEAN);
    this.value = value;
  }

  @Override
  public boolean getBoolean() {
    return value;
  }

  @Override
  public byte getByte() {
    return (byte) ConvHelpers.boolean2integer(value);
  }

  @Override
  public short getShort() {
    return (short) ConvHelpers.boolean2integer(value);
  }

  @Override
  public int getInteger() {
    return ConvHelpers.boolean2integer(value);
  }

  @Override
  public long getLong() {
    return ConvHelpers.boolean2long(value);
  }

  @Override
  public float getSingle() {
    return ConvHelpers.boolean2single(value);
  }

  @Override
  public double getDouble() {
    return ConvHelpers.boolean2double(value);
  }

  @Override
  public String getString() {
    return ConvHelpers.boolean2string(value);
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
        return IntegerVariant.getIntegerVariant(getInteger() + rightOp.getInteger());
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
        return IntegerVariant.getIntegerVariant(getInteger() - rightOp.getInteger());
  
      case VARIANT_LONG:
        return LongVariant.getLongVariant(getLong() - rightOp.getLong());
  
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
        return IntegerVariant.getIntegerVariant(getInteger() * rightOp.getInteger());
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
        return IntegerVariant.getIntegerVariant(getInteger() / rightOp.getInteger());
  
      case VARIANT_LONG:
        return LongVariant.getLongVariant(getLong() / rightOp.getLong());
  
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(getSingle()).idiv(rightOp);
  
      case VARIANT_DOUBLE:
      case VARIANT_STRING:
        return DoubleVariant.getDoubleVariant(getDouble()).idiv(rightOp);
    }
  }

  @Override
  public Variant mod(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.sub(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
        return IntegerVariant.getIntegerVariant(getInteger() % rightOp.getInteger());
  
      case VARIANT_LONG:
        return LongVariant.getLongVariant(getLong() % rightOp.getLong());
  
      case VARIANT_SINGLE:
        return SingleVariant.getSingleVariant(getSingle() % rightOp.getSingle());
  
      case VARIANT_DOUBLE:
      case VARIANT_STRING:
        return DoubleVariant.getDoubleVariant(getDouble() % rightOp.getDouble());
    }
  }

  @Override
  public Variant pow(Variant rightOp) {
    return DoubleVariant.getDoubleVariant(getDouble()).pow(rightOp);
  }

  @Override
  public Variant neg() {
    return IntegerVariant.getIntegerVariant(-getInteger());
  }

  @Override
  public Variant shl(Variant rightOp) {
    return this;
  }

  @Override
  public Variant shr(Variant rightOp) {
    return this;
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
        return getInteger() - rightOp.getInteger();
    }
  }

  @Override
  public Variant not() {
    return BooleanVariant.getBooleanVariant(!value);
  }

  @Override
  public Variant and(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.and(rightOp);
  
      case VARIANT_BOOLEAN:
        return BooleanVariant.getBooleanVariant(value & rightOp.getBoolean());
  
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
        return IntegerVariant.getIntegerVariant(getInteger() & rightOp.getInteger());
  
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getInteger() & rightOp.getLong());
    }
  }

  @Override
  public Variant or(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.or(rightOp);
  
      case VARIANT_BOOLEAN:
        return BooleanVariant.getBooleanVariant(value | rightOp.getBoolean());
  
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
        return IntegerVariant.getIntegerVariant(getInteger() | rightOp.getInteger());
  
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getInteger() | rightOp.getLong());
    }
  }

  @Override
  public Variant xor(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:  
        // Will cause a runtime error
        return super.xor(rightOp);
  
      case VARIANT_BOOLEAN:
        return BooleanVariant.getBooleanVariant(value ^ rightOp.getBoolean());
  
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
        return IntegerVariant.getIntegerVariant(getInteger() ^ rightOp.getInteger());
  
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getInteger() ^ rightOp.getLong());
    }
  }
}
