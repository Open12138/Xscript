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

import com.google.devtools.simple.runtime.errors.ConversionError;

/**
 * Helper methods for doing runtime value conversions from one type to another.
 * 
 * @author Herbert Czymontek
 */
public final class ConvHelpers {

  private ConvHelpers() {  // COV_NF_LINE
  }                        // COV_NF_LINE

  /**
   * Converts a Boolean value to an Integer value.
   * 
   * @param b  Boolean value
   * @return  Integer value
   */
  public static int boolean2integer(boolean b) {
    return b ? -1 : 0;
  }

  /**
   * Converts a Boolean value to a Long value.
   * 
   * @param b  Boolean value
   * @return  Long value
   */
  public static long boolean2long(boolean b) {
    return b ? -1L : 0L;
  }

  /**
   * Converts a Boolean value to a Single value.
   * 
   * @param b  Boolean value
   * @return  Single value
   */
  public static float boolean2single(boolean b) {
    return b ? -1F : 0F;
  }

  /**
   * Converts a Boolean value to a Double value.
   * 
   * @param b  Boolean value
   * @return  Double value
   */
  public static double boolean2double(boolean b) {
    return b ? -1D : 0D;
  }

  /**
   * Converts a Boolean value to a String value.
   * 
   * @param b  Boolean value
   * @return  String value
   */
  public static String boolean2string(boolean b) {
    return b ? "True" : "False";
  }

  /**
   * Converts a Byte value to an Integer value.
   * 
   * @param b  Byte value
   * @return  Integer value
   */
  public static int byte2integer(byte b) {
    return (b << 24) >> 24;
  }

  /**
   * Converts a Short value to a Byte value.
   * 
   * @param s  Short value
   * @return  Byte value
   */
  public static byte short2byte(short s) {
    return (byte) ((s << 24) >> 24);
  }

  /**
   * Converts an Integer value to a Boolean value.
   * 
   * @param i  Integer value
   * @return  Boolean value
   */
  public static boolean integer2boolean(int i) {
    return i != 0;
  }

  /**
   * Converts an Integer value to a Byte value.
   * 
   * @param i  Integer value
   * @return  Byte value
   */
  public static byte integer2byte(int i) {
    return (byte) ((i << 24) >> 24);
  }

  /**
   * Converts an Integer value to a Short value.
   * 
   * @param i  Integer value
   * @return  Short value
   */
  public static short integer2short(int i) {
    return (short) ((i << 16) >> 16);
  }

  /**
   * Converts a Long value to a Boolean value.
   * 
   * @param l  Long value
   * @return  Boolean value
   */
  public static boolean long2boolean(long l) {
    return l != 0;
  }

  /**
   * Converts a Long value to a Byte value.
   * 
   * @param l  Long value
   * @return  Byte value
   */
  public static byte long2byte(long l) {
    return (byte) ((l << 56) >> 56);
  }

  /**
   * Converts a Long value to a Short value.
   * 
   * @param l  Long value
   * @return  Short value
   */
  public static short long2short(long l) {
    return (short) ((l << 48) >> 48);
  }

  /**
   * Converts a Single value to a Boolean value.
   * 
   * @param f  Single value
   * @return  Boolean value
   */
  public static boolean single2boolean(float f) {
    return f != 0;
  }

  /**
   * Converts a Single value to a Byte value.
   * 
   * @param f  Single value
   * @return  Byte value
   */
  public static byte single2byte(float f) {
    return (byte) (((long) f << 56) >> 56);
  }

  /**
   * Converts a Single value to a Short value.
   * 
   * @param f  Single value
   * @return  Short value
   */
  public static short single2short(float f) {
    return (short) (((long) f << 48) >> 48);
  }

  /**
   * Converts a Double value to a Boolean value.
   * 
   * @param d  Double value
   * @return  Boolean value
   */
  public static boolean double2boolean(double d) {
    return d != 0;
  }

  /**
   * Converts a Double value to a Byte value.
   * 
   * @param d  Double value
   * @return  Byte value
   */
  public static byte double2byte(double d) {
    return (byte) (((long) d << 56) >> 56);
  }

  /**
   * Converts a Double value to a Short value.
   * 
   * @param d  Double value
   * @return  Short value
   */
  public static short double2short(double d) {
    return (short) (((long) d << 48) >> 48);
  }

  /**
   * Converts a String value to a Boolean value.
   * 
   * @param s  String value
   * @return  Boolean value
   */
  public static boolean string2boolean(String s) {
    s = s.trim();
    if (s.equals("True")) {
      return true;
    } else if (s.equals("False")) {
      return false;
    } else {
      return string2double(s) != 0;
    }
  }

  /**
   * Converts a String value to a Byte value.
   * 
   * @param s  String value
   * @return  Byte value
   */
  public static byte string2byte(String s) {
    return double2byte(string2double(s));
  }

  /**
   * Converts a String value to a Short value.
   * 
   * @param s  String value
   * @return  Short value
   */
  public static short string2short(String s) {
    return double2short(string2double(s));
  }

  /**
   * Converts a String value to an Integer value.
   * 
   * @param s  String value
   * @return  Integer value
   */
  public static int string2integer(String s) {
    return (int) string2double(s);
  }

  /**
   * Converts a String value to a Long value.
   * 
   * @param s  String value
   * @return  Long value
   */
  public static long string2long(String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException nfe) {
      // TODO: this behaves inconsistently when compared with the way string2integer works
      throw new ConversionError();
    }
  }

  /**
   * Converts a String value to a Single value.
   * 
   * @param s  String value
   * @return  Single value
   */
  public static float string2single(String s) {
    return (float) string2double(s);
  }

  /**
   * Converts a String value to a Double value.
   * 
   * @param s  String value
   * @return  Double value
   */
  public static double string2double(String s) {
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException nfe) {
      throw new ConversionError();
    }
  }
}
