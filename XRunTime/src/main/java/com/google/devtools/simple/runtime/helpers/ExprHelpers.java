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

package com.google.devtools.simple.runtime.helpers;

import java.util.regex.Pattern;

/**
 * Helper methods for performing expression operations that are too complicated
 * to inline.
 * 
 * @author Herbert Czymontek
 */
public final class ExprHelpers {

  private ExprHelpers() {  // COV_NF_LINE
  }                        // COV_NF_LINE

  /**
   * Integral division operation for floats.
   * 
   * @param leftOp  operand to divide
   * @param rightOp  operand to divide by
   * @return  integral result
   */
  public static float idiv(float leftOp, float rightOp) {
    return (float) Math.floor(leftOp / rightOp);
  }

  /**
   * Integral division operation for doubles.
   * 
   * @param leftOp  operand to divide
   * @param rightOp  operand to divide by
   * @return  integral result
   */
  public static double idiv(double leftOp, double rightOp) {
    return Math.floor(leftOp / rightOp);
  }

  /**
   * Implementation of the 'Like' operation.
   * 
   * @param string  string to compare
   * @param pattern  regular expression pattern
   * @return  {@code true} if the the string matches the pattern, {@code false}
   *          otherwise
   */
  public static boolean like(String string, String pattern) {
    return Pattern.matches(pattern, string);
  }

  /**
   * Exponentiation operation for integer operands.
   * 
   * @param leftOp  base operand
   * @param rightOp  exponent operand
   * @return  exponentiation result
   */
  public static int pow(int leftOp, int rightOp) {
    return (int) Math.pow(leftOp, rightOp);
  }

  /**
   * Exponentiation operation for float operands.
   * 
   * @param leftOp  base operand
   * @param rightOp  exponent operand
   * @return  exponentiation result
   */
  public static float pow(float leftOp, float rightOp) {
    return (float) Math.pow(leftOp, rightOp);
  }
}
