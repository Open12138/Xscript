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

package com.google.devtools.simple.compiler.expressions;

import java.math.BigDecimal;

import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.DoubleType;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.compiler.types.LongType;
import com.google.devtools.simple.classfiles.Field;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.types.ByteType;
import com.google.devtools.simple.compiler.types.ShortType;
import com.google.devtools.simple.compiler.types.SmallIntegerType;
import com.google.devtools.simple.compiler.types.SingleType;

/**
 * This class represents constant expressions of numerical values.
 * 
 * @author Herbert Czymontek
 */
public final class ConstantNumberExpression extends ConstantExpression {

  // Constant numerical value
  protected final BigDecimal value;

  /**
   * Creates a new constant expression for numerical values.
   * 
   * @param position  source code start position of expression
   * @param value  constant numerical value
   */
  public ConstantNumberExpression(long position, BigDecimal value) {
    super(position);

    this.value = value;
  }

  /**
   * Returns a new constant after converting the current constant value to a
   * constant of type Byte.
   *
   * @return  converted constant value
   */
  public ConstantNumberExpression convertToByte() {
    // Note that Java byte is unsigned, while Simple Byte is signed!
    int v = (value.intValue() << 24) >> 24;
    ConstantNumberExpression c = new ConstantNumberExpression(getPosition(), new BigDecimal(v));
    c.type = ByteType.byteType;
    return c;
  }

  /**
   * Returns a new constant after converting the current constant value to a
   * constant of type Short.
   *
   * @return  converted constant value
   */
  public ConstantNumberExpression convertToShort() {
    int v = value.shortValue();
    ConstantNumberExpression c = new ConstantNumberExpression(getPosition(), new BigDecimal(v));
    c.type = ShortType.shortType;
    return c;
  }

  /**
   * Returns a new constant after converting the current constant value to a
   * constant of type Integer.
   *
   * @return  converted constant value
   */
  public ConstantNumberExpression convertToInteger() {
    int v = value.intValue();
    ConstantNumberExpression c = new ConstantNumberExpression(getPosition(), new BigDecimal(v));
    c.type = IntegerType.integerType;
    return c;
  }

  /**
   * Returns a new constant after converting the current constant value to a
   * constant of type Long.
   *
   * @return  converted constant value
   */
  public ConstantNumberExpression convertToLong() {
    long v = value.longValue();
    ConstantNumberExpression c = new ConstantNumberExpression(getPosition(), new BigDecimal(v));
    c.type = LongType.longType;
    return c;
  }

  /**
   * Returns a new constant after converting the current constant value to a
   * constant of type Single.
   *
   * @return  converted constant value
   */
  public ConstantNumberExpression convertToSingle() {
    float v = value.floatValue();
    ConstantNumberExpression c = new ConstantNumberExpression(getPosition(), new BigDecimal(v));
    c.type = SingleType.singleType;
    return c;
  }

  /**
   * Returns a new constant after converting the current constant value to a
   * constant of type Double.
   *
   * @return  converted constant value
   */
  public ConstantNumberExpression convertToDouble() {
    double v = value.doubleValue();
    ConstantNumberExpression c = new ConstantNumberExpression(getPosition(), new BigDecimal(v));
    c.type = DoubleType.doubleType;
    return c;
  }

  /**
   * Returns the Byte value of the constant.
   * 
   * @return  Byte value
   */
  public byte getByte() {
    ConstantNumberExpression c = convertToByte();
    return c.value.byteValue();
  }

  /**
   * Returns the Short value of the constant.
   * 
   * @return  Short value
   */
  public short getShort() {
    ConstantNumberExpression c = convertToShort();
    return c.value.shortValue();
  }

  /**
   * Returns the Integer value of the constant.
   * 
   * @return  Integer value
   */
  public int getInteger() {
    ConstantNumberExpression c = convertToInteger();
    return c.value.intValue();
  }

  /**
   * Returns the Single value of the constant.
   * 
   * @return  Single value
   */
  public float getSingle() {
    ConstantNumberExpression c = convertToSingle();
    return c.value.floatValue();
  }

  /**
   * Returns the Double value of the constant.
   * 
   * @return  Double value
   */
  public double getDouble() {
    ConstantNumberExpression c = convertToDouble();
    return c.value.doubleValue();
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // This test to see whether the constant has already been resolved is very important. Otherwise
    // an already resolved type might be changed to a different type. This can happen for constant
    // data members.
    if (type == null) {
      // TODO: this is quite ugly... there must be a better way...
      try {
        // Not interested in result, just in the potential ArithmeticException
        value.byteValueExact();
        type = ByteType.byteType;
        return this;
      } catch (ArithmeticException ae0) {
        // Cannot be represented as a Byte
      }

      try {
        value.shortValueExact();
        type = ShortType.shortType;
        return this;
      } catch (ArithmeticException ae1) {
        // Cannot be represented as a Short
      }

      try {
        value.intValueExact();
        type = IntegerType.integerType;
        return this;
      } catch (ArithmeticException ae2) {
        // Cannot be represented as an Integer
      }

      try {
        value.longValueExact();
        type = LongType.longType;
      } catch (ArithmeticException ae3) {
        // Cannot be represented as a Long
        type = DoubleType.doubleType;
      }
    }

    return this;
  }

  @Override
  public void generate(Method m) {
    // TODO: this should be done in the type implementations
    if (type instanceof LongType) {
      m.generateInstrLdc2(value.longValue());
    } else if (type instanceof DoubleType) {
      m.generateInstrLdc2(value.doubleValue());
    } else if (type instanceof SingleType) {
      m.generateInstrLdc(value.floatValue());
    } else if (type instanceof SmallIntegerType) {
      m.generateInstrLdc(value.intValue());
    } else {
      m.generateInstrLdc(value.toString());
    }
  }

  @Override
  public void generate(Field f) {
    if (type instanceof LongType) {
      f.setConstantLongValue(value.longValue());
    } else if (type instanceof DoubleType) {
      f.setConstantDoubleValue(value.doubleValue());
    } else if (type instanceof SingleType) {
      f.setConstantFloatValue(value.floatValue());
    } else if (type instanceof SmallIntegerType) {
      f.setConstantIntegerValue(value.intValue());
    } else {
      f.setConstantStringValue(value.toString());
    }
  }

  @Override
  public String toString() {
    return value.toString();  // COV_NF_LINE
  }
}
