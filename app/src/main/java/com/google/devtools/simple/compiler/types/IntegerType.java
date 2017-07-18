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

import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;

/**
 * Represents the primitive Integer type.
 * 
 * @author Herbert Czymontek
 */
public final class IntegerType extends SmallIntegerType {

  /**
   * Instance of primitive Integer type. 
   */
  public static final IntegerType integerType = new IntegerType();

  // Internal name of runtime support class for Integer variants
  private static final String INTEGER_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/IntegerVariant";

  // Internal name of runtime support class for Integer reference parameters
  private static final String INTEGER_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/IntegerReferenceParameter";

  /*
   * Creates a new Integer type.
   */
  private IntegerType() {
    super(TypeKind.INTEGER);
  }

  @Override
  public int bitsize() {
    return 32;
  }

  @Override
  public void generateConversion(Method m, Type toType) {
    switch (toType.getKind()) {
      default:
        // COV_NF_START
        Compiler.internalError();
        break;
        // COV_NF_END
  
      case BOOLEAN:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "integer2boolean", "(I)Z");
        break;
  
      case BYTE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "integer2byte", "(I)B");
        break;
  
      case SHORT:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "integer2short", "(I)S");
        break;
  
      case INTEGER:
        break;
  
      case LONG:
        m.generateInstrI2l();
        break;
  
      case SINGLE:
        m.generateInstrI2f();
        break;
  
      case DOUBLE:
        m.generateInstrI2d();
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(INTEGER_VARIANT_INTERNAL_NAME,
            "getIntegerVariant",
            "(I)L" + INTEGER_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokestatic("java/lang/Integer", "toString", "(I)Ljava/lang/String;");
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrIaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrIastore();
  }

  @Override
  public void generateJavaBox(Method m) {
    m.generateInstrInvokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
  }

  @Override
  public String internalName() {
    return "I";
  }

  @Override
  public String referenceInternalName() {
    return INTEGER_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "I";
  }

  @Override
  public String toString() {
    return "Integer";
  }
}
