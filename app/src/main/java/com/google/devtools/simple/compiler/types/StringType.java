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
 * Represents the primitive String type.
 * 
 * @author Herbert Czymontek
 */
public final class StringType extends Type {

  /**
   * Instance of primitive String type. 
   */
  public static final StringType stringType = new StringType();

  // Internal name of runtime support class for String variants
  private static final String STRING_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/StringVariant";

  // Internal name of runtime support class for String reference parameters
  private static final String STRING_REFERENCE_INTERNAL_NAME =
      REFERENCE_PACKAGE_INTERNAL_NAME + "/StringReferenceParameter";

  /*
   * Creates a new String type.
   */
  private StringType() {
    super(TypeKind.STRING);
  }

  @Override
  public boolean isScalarType() {
    return true;
  }

  @Override
  public boolean canConvertTo(Type toType) {
    switch (toType.getKind()) {
      default:
        return false;
  
      case BOOLEAN:
      case BYTE:
      case SHORT:
      case INTEGER:
      case LONG:
      case SINGLE:
      case DOUBLE:
      case STRING:
      case VARIANT:
        return true;
    }
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrLdc("");
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
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2boolean",
            "(Ljava/lang/String;)Z");
        break;
  
      case BYTE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2byte",
            "(Ljava/lang/String;)B");
        break;
  
      case SHORT:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2short",
            "(Ljava/lang/String;)S");
        break;
  
      case INTEGER:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2integer",
            "(Ljava/lang/String;)I");
        break;
  
      case LONG:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2long",
            "(Ljava/lang/String;)J");
        break;
  
      case SINGLE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2single",
            "(Ljava/lang/String;)F");
        break;
  
      case DOUBLE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "string2double",
            "(Ljava/lang/String;)D");
        break;
  
      case STRING:
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(STRING_VARIANT_INTERNAL_NAME, "getStringVariant",
            "(Ljava/lang/String;)L" + STRING_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
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
  public void generateLoadArray(Method m) {
    m.generateInstrAaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrAastore();
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrAreturn();
  }
  
  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z");
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z");
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateLike(Method m) {
    m.generateInstrInvokestatic(EXPRESSION_HELPERS_INTERNAL_NAME, "like",
        "(Ljava/lang/String;Ljava/lang/String;)Z");
  }

  @Override
  public String internalName() {
    return "java/lang/String";
  }

  @Override
  public String referenceInternalName() {
    return STRING_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return signature();
  }

  @Override
  public String signature() {
    return "Ljava/lang/String;";
  }
  
  @Override
  public String toString() {
    return "String";
  }
}
