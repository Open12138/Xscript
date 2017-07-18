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

package com.google.devtools.simple.runtime;

import com.google.devtools.simple.runtime.annotations.SimpleDataElement;
import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.variants.IntegerVariant;
import com.google.devtools.simple.runtime.variants.Variant;

import java.util.Random;
import java.security.SecureRandom;

/**
 * Implementation of various mathematical runtime functions.
 *
 * @author Herbert Czymontek
 */
@SimpleObject
public final class Math {

  private final static Random randomGenerator = new SecureRandom();

  /**
   * Euler's constant.
   */
  @SimpleDataElement
  public static final double E = java.lang.Math.E;

  /**
   * Pi.
   */
  @SimpleDataElement
  public static final double PI = java.lang.Math.PI;

  private Math() {  // COV_NF_LINE
  }                 // COV_NF_LINE

  /**
   * Returns the absolute value of the given value.
   *
   * @param v  value
   * @return  absolute value
   */
  @SimpleFunction
  public static Variant Abs(Variant v) {
    if (v.cmp(IntegerVariant.getIntegerVariant(0)) < 0) {
      v = v.mul(IntegerVariant.getIntegerVariant(-1));
    }

    return v;
  }

  /**
   * Returns the arctangent for the given value.
   *
   * @param v  value
   * @return  arctangent of {@code v}
   */
  @SimpleFunction
  public static double Atn(double v) {
    return java.lang.Math.atan(v);
  }

 /**
  * Returns the angle theta from the conversion of rectangular
  * coordinates (x, y) to polar coordinates (r, theta>).
  * @param   y   the ordinate coordinate
  * @param   x   the abscissa coordinate
  * @return  the theta component of the point
  *          (r, theta) in polar coordinates that corresponds to the point
  *          (x, y) in Cartesian coordinates
  */
  @SimpleFunction
  public static double Atn2(double y, double x) {
    return java.lang.Math.atan2(y,x);
  }

  /**
   * Returns the cosine for the given value.
   *
   * @param v value
   * @return  cosine of {@code v}
   */
  @SimpleFunction
  public static double Cos(double v) {
    return java.lang.Math.cos(v);
  }

  /**
   * Returns e (euler's constant) raised to the power of the given value.
   *
   * @param v  value
   * @return  e to the power of {@code v}
   */
  @SimpleFunction
  public static double Exp(double v) {
    return java.lang.Math.exp(v);
  }

  /**
   * Returns the integer part of the given number.
   *
   * @param v  value
   * @return  integer part of {@code v}
   */
  @SimpleFunction
  public static long Int(Variant v) {
    return v.getLong();
  }

  /**
   * Returns the natural logarithm for the given number.
   *
   * @param v  value
   * @return  natural logarithm for {@code v}
   */
  @SimpleFunction
  public static double Log(double v) {
    return java.lang.Math.log(v);
  }

  /**
   * Returns the greater of two values.
   *
   * @param v1  first value
   * @param v2  second value
   * @return  greater value of {@code v1} and {@code v2}
   */
  @SimpleFunction
  public static Variant Max(Variant v1, Variant v2) {
    // Need to make sure variants contain numeric values: invoking getDouble() will cause a runtime
    // error if not
    v1.getDouble();
    v2.getDouble();

    return v1.cmp(v2) < 0 ? v2 : v1;
  }

  /**
   * Returns the smaller of two values.
   *
   * @param v1  first value
   * @param v2  second value
   * @return  smaller value of {@code v1} and {@code v2}
   */
  @SimpleFunction
  public static Variant Min(Variant v1, Variant v2) {
    // Need to make sure variants contain numeric values: invoking getDouble() will cause a runtime
    // error if not
    v1.getDouble();
    v2.getDouble();

    return v1.cmp(v2) >= 0 ? v2 : v1;
  }

  /**
   * Returns a random number in the range between 0.0 (inclusive) and 1.0
   * (exclusive).
   *
   * @return  random number (between 0.0 and 1.0)
   */
  @SimpleFunction
  public static double Rnd() {
    return randomGenerator.nextDouble();
  }

  /**
   * Returns the sine for the given value.
   *
   * @param v  value
   * @return  sine of {@code v}
   */
  @SimpleFunction
  public static double Sin(double v) {
    return java.lang.Math.sin(v);
  }

  /**
   * Indicates the sign for the given value.
   *
   * @param v  value
   * @return  1 for positive values, 0 for zero, and -1 for negative values
   */
  @SimpleFunction
  public static int Sgn(double v) {
    return (int)java.lang.Math.signum(v);
  }

  /**
   * Returns the square root for the given value.
   *
   * @param v  value
   * @return  square root of {@code v}
   */
  @SimpleFunction
  public static double Sqr(double v) {
    return java.lang.Math.sqrt(v);
  }

  /**
   * Returns the tangent for the given value.
   *
   * @param v  value
   * @return  tangent of {@code v}
   */
  @SimpleFunction
  public static double Tan(double v) {
    return java.lang.Math.tan(v);
  }

  /**
   * Converts an angle measured in degrees to an approximation in radians.
   *
   * @param d value in degrees
   * @return radian approximation to {@code d} degrees
   */
  @SimpleFunction
  public static double DegreesToRadians(double d) {
    return java.lang.Math.toRadians(d);
  }

  /**
   * Converts an angle measured in radians to an approximation in degrees.
   *
   * @param r value in radians
   * @return degree approximation to {@code r} radians
   */
  @SimpleFunction
  public static double RadiansToDegrees(double r) {
    return java.lang.Math.toDegrees(r);
  }
}
