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

import com.google.devtools.simple.runtime.errors.UnknownIdentifierError;
import com.google.devtools.simple.runtime.helpers.ConvHelpers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Object variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class ObjectVariant extends Variant {

  // Object value
  private Object value;

  /**
   * Factory method for creating object variants.
   * 
   * @param value  object value
   * @return  new object variant
   */
  public static final ObjectVariant getObjectVariant(Object value) {
    return new ObjectVariant(value);
  }

  /*
   * Creates a new object variant.
   */
  private ObjectVariant(Object value) {
    super(VARIANT_OBJECT);
    this.value = value;
  }

  @Override
  public Object getObject() {
    return value;
  }

  @Override
  public boolean identical(Variant rightOp) {
    if (rightOp.getKind() != VARIANT_OBJECT) {
      // Will cause a runtime error
      return super.identical(rightOp);
    }

    return value == rightOp.getObject();
  }

  /*
   * Converts a variant value to its equivalent Java value.
   */
  private Object convertVariant(Class<?> type, Variant variant) {
    if (type.isArray()) {
      return variant.getArray();
    }
    if (type == Boolean.TYPE) {
      return variant.getBoolean();
    }
    if (type == Byte.TYPE) {
      return variant.getByte();
    }
    if (type == Short.TYPE) {
      return variant.getShort();
    }
    if (type == Integer.TYPE) {
      return variant.getInteger();
    }
    if (type == Long.TYPE) {
      return variant.getLong();
    }
    if (type == Float.TYPE) {
      return variant.getSingle();
    }
    if (type == Double.TYPE) {
      return variant.getDouble();
    }
    if (type == String.class) {
      return variant.getString();
    }

    return variant.getObject();
  }

  /*
   * Converts a Java value to a variant value.
   */
  private Variant convertObject(Class<?> type, Object object) {
    if (type == Void.TYPE) {
      return UninitializedVariant.getUninitializedVariant();
    }
    if (type == Boolean.TYPE) {
      return BooleanVariant.getBooleanVariant(((Boolean)object).booleanValue());
    }
    if (type == Byte.TYPE) {
      return ByteVariant.getByteVariant(ConvHelpers.integer2byte(((Byte)object).intValue()));
    }
    if (type == Short.TYPE) {
      return ShortVariant.getShortVariant(ConvHelpers.integer2short(((Short)object).intValue()));
    }
    if (type == Integer.TYPE) {
      return IntegerVariant.getIntegerVariant(((Integer)object).intValue());
    }
    if (type == Long.TYPE) {
      return LongVariant.getLongVariant(((Long)object).longValue());
    }
    if (type == Float.TYPE) {
      return SingleVariant.getSingleVariant(((Float)object).floatValue());
    }
    if (type == Double.TYPE) {
      return DoubleVariant.getDoubleVariant(((Double)object).doubleValue());
    }
    if (type == String.class) {
      return StringVariant.getStringVariant((String)object);
    }
    if (type.isArray()) {
      return ArrayVariant.getArrayVariant(object);
    }

    return ObjectVariant.getObjectVariant(object);
  }

  /*
   * Converts the function arguments to proper Java method arguments.
   */
  private Object[] convertArguments(Class<?>[] argTypes, Variant[] args) {
    int len = argTypes.length;
    Object[] objArgs = new Object[len];
    for (int i = 0; i < len; i++) {
      objArgs[i] = convertVariant(argTypes[i], args[i]);
    }
    return objArgs;
  }
  
  @Override
  public Variant function(String name, Variant[] args) {
    Class<?> type = value.getClass();
    
    // Because we don't have a definitive list with parameter types we need to get the entire
    // list of methods and then look for the method we are interested. Note that Simple doesn't
    // allow for method overloading.
    for (Method method : type.getMethods()) {
      if (method.getName().equals(name)) {
        Class<?>[] argTypes = method.getParameterTypes();
        if (argTypes.length == args.length) {
          // Convert actual arguments
          Object[] convertedArgs = convertArguments(argTypes, args);
  
          // Invoke method
          Object result = null;
          try {
            result = method.invoke(value, convertedArgs);
          } catch (IllegalAccessException iae) {
            // Cannot happen - all Simple methods are public
          } catch (InvocationTargetException ite) {
            throw new UnknownIdentifierError();
          }
  
          // Convert result to variant
          return convertObject(method.getReturnType(), result);
        }
      }
    }

    // No matching method found
    throw new UnknownIdentifierError();
  }

  @Override
  public Variant dataMember(String name) {
    Class<?> type = value.getClass();
    try {
      Field field = type.getField(name);
      return convertObject(field.getType(), field.get(value));
    } catch (NoSuchFieldException nsfe) {
      // So there is no data member of that name, but there could still be a property getter
      return function(name, new Variant[0]);
    } catch (IllegalAccessException iae) {
      // Cannot happen - all Simple fields are public
    }
    // Should never get here.
    return null;
  }

  @Override
  public void dataMember(String name, Variant variant) {
    Class<?> type = value.getClass();
    try {
      Field field = type.getField(name);
      field.set(value, convertVariant(field.getType(), variant));
    } catch (NoSuchFieldException nsfe) {
      // So there is no data member of that name, but there could still be a property setter
      function(name, new Variant[] { variant });
    } catch (IllegalAccessException iae) {
      // Cannot happen - all Simple fields are public
    }
  }
}
