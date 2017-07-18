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
 * Long variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class LongVariant extends Variant {

  // Long value
  private long value;

  /**
   * Factory method for creating Long variants.
   * 
   * @param value  Long value
   * @return  new Long variant
   */
  public static final LongVariant getLongVariant(long value) {
    return new LongVariant(value);
  }

  /*
   * Creates a new Long variant.
   */
  private LongVariant(long value) {
    super(VARIANT_LONG);
    this.value = value;
  }

  @Override
  public boolean getBoolean() {
    return ConvHelpers.long2boolean(value);
  }

  @Override
  public byte getByte() {
    return ConvHelpers.long2byte(value);
  }

  @Override
  public short getShort() {
    return ConvHelpers.long2short(value);
  }

  @Override
  public int getInteger() {
    return (int) value;
  }

  @Override
  public long getLong() {
    return value;
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
    return Long.toString(value);
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
        return LongVariant.getLongVariant(getLong() + rightOp.getLong());
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
      case VARIANT_LONG:
        return LongVariant.getLongVariant(getLong() * rightOp.getLong());
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
        return LongVariant.getLongVariant(getLong() / rightOp.getLong());
  
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
    return LongVariant.getLongVariant(-value);
  }

  @Override
  public Variant shl(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.and(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getLong() << rightOp.getLong());
    }
  }

  @Override
  public Variant shr(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.and(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getLong() >> rightOp.getLong());
    }
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
        long rvalue = rightOp.getLong();
        if (value == rvalue) {
          return 0;
        }
        return value > rvalue ? 1 : -1;
    }
  }

  @Override
  public Variant not() {
    return getLongVariant(~value);
  }

  @Override
  public Variant and(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.and(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getLong() & rightOp.getLong());
    }
  }

  @Override
  public Variant or(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.or(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getLong() | rightOp.getLong());
    }
  }

  @Override
  public Variant xor(Variant rightOp) {
    switch (rightOp.getKind()) {
      default:
        // Will cause a runtime error
        return super.xor(rightOp);
  
      case VARIANT_BOOLEAN:
      case VARIANT_BYTE:
      case VARIANT_SHORT:
      case VARIANT_INTEGER:
      case VARIANT_LONG:
      case VARIANT_STRING:
        return LongVariant.getLongVariant(getLong() ^ rightOp.getLong());
    }
  }
}
