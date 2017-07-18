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

import com.google.devtools.simple.runtime.errors.ConversionError;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Superclass for the variant runtime support classes.
 * 
 * <p>Note that all variant implementations use factory methods to create new
 * instances because this will allow the compiler to generate more compact
 * code. 
 * 
 * @author Herbert Czymontek
 */
public abstract class Variant {

  /*
   * Helpers for TypeOf operation
   */
  private static interface TypeOfChecker {
    boolean check(Variant v);
  }

  static Map<String, TypeOfChecker> TYPEOF_CHECKER_MAP = new HashMap<String, TypeOfChecker>();
  static {
    TYPEOF_CHECKER_MAP.put("java/lang/String", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getString();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("java/util/Calendar", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getDate();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("Z", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getBoolean();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("B", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getByte();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("S", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getShort();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("I", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getInteger();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("J", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getLong();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("F", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getSingle();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });

    TYPEOF_CHECKER_MAP.put("D", new TypeOfChecker() {
      @Override
      public boolean check(Variant v) {
        try {
          v.getDouble();
        } catch (ConversionError e) {
          return false;
        }
        return true;
      }
    });
  }
  
  /**
   * Base type of variant (using bytes instead of enums to be more compact).
   */
  protected static final byte VARIANT_UNINITIALIZED = 0;
  protected static final byte VARIANT_BOOLEAN = 1;
  protected static final byte VARIANT_BYTE = 2;
  protected static final byte VARIANT_SHORT = 3;
  protected static final byte VARIANT_INTEGER = 4;
  protected static final byte VARIANT_LONG = 5;
  protected static final byte VARIANT_SINGLE = 6;
  protected static final byte VARIANT_DOUBLE = 7;
  protected static final byte VARIANT_STRING = 8;
  protected static final byte VARIANT_OBJECT = 9;
  protected static final byte VARIANT_ARRAY = 10;
  protected static final byte VARIANT_DATE = 11;

  // Variant type kind
  private final byte kind;

  /**
   * Creates a new variant.
   * 
   * @param kind  kind of variant
   */
  protected Variant(byte kind) {
    this.kind = kind;
  }

  /**
   * Returns the kind of variant.
   * 
   * @return  variant kind
   */
  protected final byte getKind() {
    return kind;
  }

  /**
   * Returns the variant value as a Simple Boolean value.
   * 
   * @return  Boolean value
   */
  public boolean getBoolean() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Byte value.
   * 
   * @return  Byte value
   */
  public byte getByte() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Short value.
   * 
   * @return  Short value
   */
  public short getShort() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Integer value.
   * 
   * @return  Integer value
   */
  public int getInteger() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Long value.
   * 
   * @return  Long value
   */
  public long getLong() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Single value.
   * 
   * @return  Single value
   */
  public float getSingle() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Double value.
   * 
   * @return  Double value
   */
  public double getDouble() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple String value.
   * 
   * @return  String value
   */
  public String getString() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple object.
   * 
   * @return  object
   */
  public Object getObject() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple array.
   * 
   * @return  array
   */
  public Object getArray() {
    throw new ConversionError();
  }

  /**
   * Returns the variant value as a Simple Date.
   * 
   * @return  date
   */
  public Calendar getDate() {
    throw new ConversionError();
  }

  /**
   * Performs an addition of the variant values.
   * 
   * @param rightOp  right operand for addition
   * @return  variant containing result of addition
   */
  public Variant add(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a subtraction of the variant values.
   * 
   * @param rightOp  right operand for subtraction
   * @return  variant containing result of subtraction
   */
  public Variant sub(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a multiplication of the variant values.
   * 
   * @param rightOp  right operand for multiplication
   * @return  variant containing result of multiplication
   */
  public Variant mul(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a division of the variant values.
   * 
   * @param rightOp  right operand for division
   * @return  variant containing result of division
   */
  public Variant div(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs an integral division of the variant values.
   * 
   * @param rightOp  right operand for division
   * @return  variant containing result of division
   */
  public Variant idiv(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a modulo operation of the variant values.
   * 
   * @param rightOp  right operand for modulo operation
   * @return  variant containing result of modulo operation
   */
  public Variant mod(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs an exponentiation of the variant values.
   * 
   * @param rightOp  right operand for exponentiation
   * @return  variant containing result of exponentiation
   */
  public Variant pow(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a negation of the variant value.
   * 
   * @return  variant containing result of negation
   */
  public Variant neg() {
    throw new ConversionError();
  }

  /**
   * Performs a shift left of the variant value.
   * 
   * @param rightOp  right operand for shift left
   * @return  variant containing result of shift left
   */
  public Variant shl(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a shift right of the variant value.
   * 
   * @param rightOp  right operand for shift right
   * @return  variant containing result of shift right
   */
  public Variant shr(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a comparison of the variant values.
   * 
   * @param rightOp  right operand for comparison
   * @return  negative result if rightOp value is greater than this value, 0 if
   *          the values are equal, and positive result of this value is
   *          greater than rightOp value 
   */
  public int cmp(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a comparison of the variant values.
   * 
   * @param rightOp  right operand for comparison
   * @return  {@code true} if the values are identical, {@code false} otherwise
   */
  public boolean identical(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a 'Like' operation with the variant values.
   * 
   * @see com.google.devtools.simple.runtime.helpers.ExprHelpers#like(String, String)
   * 
   * @param rightOp  right operand (regular expression)
   * @return  {@code true} if this value is a match of the regular expression,
   *          {@code false} otherwise
   */
  public boolean like(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs a type check of the variant value.
   * 
   * @param type  internal name of expected type
   * @return  {@code true} if the types match, {@code false} otherwise
   */
  public boolean typeof(String internalName) {
    TypeOfChecker checker = TYPEOF_CHECKER_MAP.get(internalName);
    if (checker != null) {
      return checker.check(this);
    }

    try {
      return Class.forName(internalName.replace('/', '.')).isInstance(kind == VARIANT_ARRAY ?
          getArray() : getObject());
    } catch (ConversionError e) {
      return false;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Performs a NOT operation on the variant value.
   * 
   * @return  variant containing result of the NOT operation
   */
  public Variant not() {
    throw new ConversionError();
  }

  /**
   * Performs an AND operation of the variant values.
   * 
   * @param rightOp  right operand for AND operation
   * @return  variant containing result of the AND operation
   */
  public Variant and(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs an OR operation of the variant values.
   * 
   * @param rightOp  right operand for OR operation
   * @return  variant containing result of the OR operation
   */
  public Variant or(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Performs an XOR operation of the variant values.
   * 
   * @param rightOp  right operand for XOR operation
   * @return  variant containing result of the XOR operation
   */
  public Variant xor(Variant rightOp) {
    throw new ConversionError();
  }

  /**
   * Invokes the function using the variant as a 'Me' argument.
   * 
   * @param name  function name
   * @param args  function arguments
   * @return  function result or {@code null} for procedures
   */
  public Variant function(String name, Variant[] args) {
    throw new ConversionError();
  }

  /**
   * Reads the value of the data member using the variant as a 'Me' argument.
   * 
   * @param name  data member name
   * @return  data member value
   */
  public Variant dataMember(String name) {
    throw new ConversionError();
  }

  /**
   * Writes to the data member using the variant as a 'Me' argument.
   * 
   * @param name  data member name
   * @param variant  value to write to data member
   */
  public void dataMember(String name, Variant variant) {
    throw new ConversionError();
  }

  /**
   * Reads the value of an array element.
   * 
   * @param indices  indices for the array
   * @return  value of array element
   */
  public Variant array(Variant[] indices) {
    throw new ConversionError();
  }

  /**
   * Writes a value to an array element.
   * 
   * @param indices  indices for the array
   * @param variant  value to write
   */
  public void array(Variant[] indices, Variant variant) {
    throw new ConversionError();
  }
}
