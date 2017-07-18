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
 * Represents the Date type.
 * 
 * @author Herbert Czymontek
 */
public final class DateType extends Type {

  /**
   * Instance of Date type. 
   */
  public static final DateType dateType = new DateType();

  // Internal name of runtime support class for Date variants
  private static final String DATE_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/DateVariant";

  // Internal name of runtime support class for Date reference parameters
  private static final String DATE_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/DateReferenceParameter";

  // Date implementor internal name
  private static final String DATE_INTERNAL_NAME = "java/util/Calendar";

  // Date implementor signature
  private static final String DATE_SIGNATURE = "L" + DATE_INTERNAL_NAME + ";";
  
  // compareTo() signature
  private static final String COMPARE_TO_SIGNATURE = "(" + DATE_SIGNATURE + ")I";

  /*
   * Creates a new Date type.
   */
  private DateType() {
    super(TypeKind.DATE);
  }

  @Override
  public boolean canConvertTo(Type toType) {
    return toType.isVariantType();
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrAconstNull();
  }

  @Override
  public void generateConversion(Method m, Type toType) {
    switch (toType.getKind()) {
      default:
        // COV_NF_START
        Compiler.internalError();
        break;
        // COV_NF_END
  
      case VARIANT:
        m.generateInstrInvokestatic(DATE_VARIANT_INTERNAL_NAME,
            "getDateVariant",
            "(" + DATE_SIGNATURE + ")L" + DATE_VARIANT_INTERNAL_NAME + ";");
        break;
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
    m.generateInstrInvokevirtual(internalName(), "compareTo", COMPARE_TO_SIGNATURE);
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(internalName(), "compareTo", COMPARE_TO_SIGNATURE);
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(internalName(), "compareTo", COMPARE_TO_SIGNATURE);
    m.generateInstrIflt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(internalName(), "compareTo", COMPARE_TO_SIGNATURE);
    m.generateInstrIfle(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(internalName(), "compareTo", COMPARE_TO_SIGNATURE);
    m.generateInstrIfgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(internalName(), "compareTo", COMPARE_TO_SIGNATURE);
    m.generateInstrIfge(label);
  }

  @Override
  public String internalName() {
    return DATE_INTERNAL_NAME;
  }

  @Override
  public String signature() {
    return DATE_SIGNATURE;
  }

  @Override
  public String referenceInternalName() {
    return DATE_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return signature();
  }

  @Override
  public String toString() {
    return "Date";
  }
}
