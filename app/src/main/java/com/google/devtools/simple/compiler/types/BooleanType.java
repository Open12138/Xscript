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
import com.google.devtools.simple.classfiles.Method;

/**
 * Represents the primitive Boolean type.
 * 
 * @author Herbert Czymontek
 */
public final class BooleanType extends Type {

  /**
   * Instance of primitive Boolean type. 
   */
  public static final BooleanType booleanType = new BooleanType();

  // Internal name of runtime support class for Boolean variants
  private static final String BOOLEAN_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/BooleanVariant";

  // Internal name of runtime support class for Boolean reference parameters
  private static final String BOOLEAN_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/BooleanReferenceParameter";

  /*
   * Creates a new Boolean type.
   */
  private BooleanType() {
    super(TypeKind.BOOLEAN);
  }

  @Override
  public boolean isBooleanType() {
    return true;
  }

  @Override
  public boolean isScalarIntegerType() {
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
    m.generateInstrLdc(0);
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
        break;
  
      case BYTE:
      case SHORT:
      case INTEGER:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "boolean2integer", "(Z)I");
        break;
  
      case LONG:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "boolean2long", "(Z)J");
        break;
  
      case SINGLE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "boolean2single", "(Z)F");
        break;
  
      case DOUBLE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "boolean2double", "(Z)D");
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(BOOLEAN_VARIANT_INTERNAL_NAME,
            "getBooleanVariant",
            "(Z)L" + BOOLEAN_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "boolean2string",
        "(Z)Ljava/lang/String;");
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrIload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrIstore(varIndex);
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrBaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrBastore();
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrIreturn();
  }

  @Override
  public void generateBitNot(Method m) {
    m.generateInstrLdc(1);
    m.generateInstrIxor();
  }

  @Override
  public void generateBitXor(Method m) {
    m.generateInstrIxor();
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmpeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmpne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrIfIcmplt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmple(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrIfIcmpgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmpge(label);
  }

  @Override
  public void generateJavaBox(Method m) {
    m.generateInstrInvokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
  }

  @Override
  public String internalName() {
    return "Z";
  }

  @Override
  public String referenceInternalName() {
    return BOOLEAN_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "Z";
  }

  @Override
  public String toString() {
    return "Boolean";
  }
}
