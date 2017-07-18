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

package com.google.devtools.simple.compiler.expressions.synthetic;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.ConstantNumberExpression;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.expressions.NothingExpression;
import com.google.devtools.simple.compiler.expressions.UnaryExpression;
import com.google.devtools.simple.compiler.types.ByteType;
import com.google.devtools.simple.compiler.types.DoubleType;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.compiler.types.LongType;
import com.google.devtools.simple.compiler.types.ShortType;
import com.google.devtools.simple.compiler.types.SingleType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.classfiles.Method;

/**
 * This class represents a conversion expression.
 * 
 * @author Herbert Czymontek
 */
public final class ConversionExpression extends UnaryExpression {

  private ConversionExpression(Expression operand, Type type) {
    super(operand.getPosition(), operand);
    this.type = type;
  }

  /**
   * Converts the type of the given expression to a new type.
   *
   * @param compiler  current compiler instance
   * @param operand  expression whose type needs conversion
   * @param toType  required type
   * @return  expression with the required type
   */
  public static Expression convert(Compiler compiler, Expression operand, Type toType) {
    // Nothing to do if the type is already correct
    Type fromType = operand.getType();
    if (fromType.equals(toType)) {
      return operand;
    }

    // Nothing doesn't really need to be converted - just change its type
    if (operand instanceof NothingExpression) {
      ((NothingExpression) operand).changeType(toType);
      return operand;
    }
    
    // Constant numerical expressions can be converted directly
    if (operand instanceof ConstantNumberExpression) {
      ConstantNumberExpression constExpr = (ConstantNumberExpression) operand;
      if (toType.equals(ByteType.byteType)) {
        return constExpr.convertToByte();
      }
      if (toType.equals(ShortType.shortType)) {
        return constExpr.convertToShort();
      }
      if (toType.equals(IntegerType.integerType)) {
        return constExpr.convertToInteger();
      }
      if (toType.equals(LongType.longType)) {
        return constExpr.convertToLong();
      }
      if (toType.equals(SingleType.singleType)) {
        return constExpr.convertToSingle();
      }
      if (toType.equals(DoubleType.doubleType)) {
        return constExpr.convertToDouble();
      }
    }

    // Check whether this is a legal conversion (might still fail at runtime though)
    if (!fromType.canConvertTo(toType)) {
      compiler.error(operand.getPosition(), Error.errCannotConvertType, fromType.toString(),
          toType.toString());
    }

    // Create conversion expression to perform conversion at runtime
    return new ConversionExpression(operand, toType);
  }

  @Override
  public void generate(Method m) {
    operand.generate(m);
    operand.getType().generateConversion(m, type);
  }
}
