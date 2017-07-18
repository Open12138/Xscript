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

import java.lang.reflect.Array;

/**
 * Array variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class ArrayVariant extends Variant {

  // Array value
  private Object value;

  /**
   * Factory method for creating array variants.
   * 
   * @param value  array
   * @return  new array variant
   */
  public static final ArrayVariant getArrayVariant(Object value) {
    return new ArrayVariant(value);
  }

  /*
   * Creates a new array variant.
   */
  private ArrayVariant(Object value) {
    super(VARIANT_ARRAY);
    this.value = value;
  }

  @Override
  public Object getArray() {
    return value;
  }

  @Override
  public boolean identical(Variant rightOp) {
    if (rightOp.getKind() != VARIANT_ARRAY) {
      // Will cause a runtime error
      return super.identical(rightOp);
    }

    return value == rightOp.getArray();
  }

  /*
   * Returns the array object for the last dimension. 
   */
  private Object getArrayOfLastDimension(Variant[] indices) {
    int lastDimension = indices.length - 1;
    Object array = value;
    for (int i = 0; i < lastDimension; i++) {
      array = Array.get(array, indices[i].getInteger());
    }
    return array;
  }
  
  @Override
  public Variant array(Variant[] indices) {
    Object array = getArrayOfLastDimension(indices);
    Class<?> elementType = array.getClass().getComponentType();
    int lastIndex = indices[indices.length - 1].getInteger();

    if (elementType == boolean.class) {
      return BooleanVariant.getBooleanVariant(Array.getBoolean(array, lastIndex));
    } else if (elementType == byte.class) {
      return ByteVariant.getByteVariant(Array.getByte(array, lastIndex));
    } else if (elementType == short.class) {
      return ShortVariant.getShortVariant(Array.getShort(array, lastIndex));
    } else if (elementType == int.class) {
      return IntegerVariant.getIntegerVariant(Array.getInt(array, lastIndex));
    } else if (elementType == long.class) {
      return LongVariant.getLongVariant(Array.getLong(array, lastIndex));
    } else if (elementType == float.class) {
      return SingleVariant.getSingleVariant(Array.getFloat(array, lastIndex));
    } else if (elementType == double.class) {
      return DoubleVariant.getDoubleVariant(Array.getDouble(array, lastIndex));
    } else if (elementType == String.class) {
      return StringVariant.getStringVariant((String) Array.get(array, lastIndex));
    } else {
      return ObjectVariant.getObjectVariant(Array.get(array, lastIndex));
    }
  }

  @Override
  public void array(Variant[] indices, Variant variant) {
    Object array = getArrayOfLastDimension(indices);
    Class<?> elementType = array.getClass().getComponentType();
    int lastIndex = indices[indices.length - 1].getInteger();

    if (elementType == boolean.class) {
      Array.set(array, lastIndex, variant.getBoolean());
    } else if (elementType == byte.class) {
      Array.set(array, lastIndex, variant.getByte());
    } else if (elementType == short.class) {
      Array.set(array, lastIndex, variant.getShort());
    } else if (elementType == int.class) {
      Array.set(array, lastIndex, variant.getInteger());
    } else if (elementType == long.class) {
      Array.set(array, lastIndex, variant.getLong());
    } else if (elementType == float.class) {
      Array.set(array, lastIndex, variant.getSingle());
    } else if (elementType == double.class) {
      Array.set(array, lastIndex, variant.getDouble());
    } else if (elementType == String.class) {
      Array.set(array, lastIndex, variant.getString());
    } else {
      Array.set(array, lastIndex, variant.getObject());
    }
  }
}
