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

package com.google.devtools.simple.compiler.types;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.util.Preconditions;
import com.google.devtools.simple.classfiles.Method;

/**
 * Represents array types in Simple.
 * 
 * @author Herbert Czymontek
 */
public final class ArrayType extends Type {

  // Internal name of runtime support class for String variants
  private static final String ARRAY_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/ArrayVariant";

  // Internal name of runtime support class for array reference parameters
  private static final String ARRAY_REFERENCE_INTERNAL_NAME =
      REFERENCE_PACKAGE_INTERNAL_NAME + "/ObjectReferenceParameter";

  // Type of the array elements
  private final Type elementType;

  // Number of array dimensions
  private final int dimensions;

  /**
   * Creates a new array type.
   * 
   * @param elementType  type of array elements
   * @param dimensions  array dimensions
   */
  public ArrayType(Type elementType, int dimensions) {
    super(TypeKind.ARRAY);

    Preconditions.checkArgument(!elementType.isArrayType() && !elementType.isFunctionType());

    this.elementType = elementType;
    this.dimensions = dimensions;
  }

  @Override
  protected boolean typeEquals(Type type) {
    if (!(type instanceof ArrayType)) {
      return false;
    }

    ArrayType arrayType = (ArrayType) type;
    return dimensions == arrayType.dimensions && elementType.equals(arrayType.elementType);
  }

  @Override
  public boolean isArrayType() {
    return true;
  }

  @Override
  public boolean canConvertTo(Type toType) {
    return toType.isVariantType();
  }

  /**
   * Returns the type of the array elements.
   * 
   * @return  array element type
   */
  public Type getElementType() {
    return elementType;
  }

  /**
   * Returns the number of array dimensions.
   * 
   * @return  array dimensions
   */
  public int getDimensions() {
    return dimensions;
  }

  @Override
  public void resolve(Compiler compiler) {
    elementType.resolve(compiler);
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrAconstNull();
  }

  @Override
  public void generateConversion(Method m, Type toType) {
    if (toType.isVariantType()) {
      m.generateInstrInvokestatic(ARRAY_VARIANT_INTERNAL_NAME, "getArrayVariant",
          "(Ljava/lang/Object;)L" + ARRAY_VARIANT_INTERNAL_NAME + ";");
    } else {
      Compiler.internalError();  // COV_NF_LINE
    }
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrAload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrAstore(varIndex);
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrAreturn();
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrIfAcmpeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrIfAcmpne(label);
  }

  @Override
  public void generateBranchIfCmpIdentical(Method m, Method.Label label) {
    m.generateInstrIfAcmpeq(label);
  }

  @Override
  public void generateBranchIfCmpNotIdentical(Method m, Method.Label label) {
    m.generateInstrIfAcmpne(label);
  }

  @Override
  public void generateTypeOf(Method m, String internalName) {
    m.generateInstrInstanceof(internalName);
  }

  @Override
  public void generateAllocateArray(Method m) {
    Preconditions.checkArgument(dimensions > 0);

    if (dimensions == 1) {
      switch (elementType.getKind()) {
        default:
          // COV_NF_START
          Compiler.internalError();
          break;
          // COV_NF_END

        case OBJECT:
        case STRING:
        case VARIANT:
          m.generateInstrAnewarray(elementType.internalName());
          break;

        case BOOLEAN:
        case BYTE:
        case DOUBLE:
        case INTEGER:
        case LONG:
        case SHORT:
        case SINGLE:
          m.generateInstrNewarray(elementType.internalName());
          break;
      }
    } else {
      m.generateInstrMultianewarray(dimensions, internalName());
    }
  }

  @Override
  public String internalName() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < dimensions; i++) {
      sb.append('[');
    }
    sb.append(elementType.signature());

    return sb.toString();
  }

  @Override
  public String referenceInternalName() {
    return ARRAY_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "Ljava/lang/Object;";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(elementType.toString());
    sb.append('(');
    for (int i = 1; i < dimensions; i++) {
      sb.append(',');
    }
    sb.append(')');
    return sb.toString();
  }
}
