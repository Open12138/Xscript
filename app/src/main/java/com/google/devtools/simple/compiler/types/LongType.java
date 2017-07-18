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
 * Represents the primitive Long type.
 * 
 * @author Herbert Czymontek
 */
public final class LongType extends IntegerNumberType {

  /**
   * Instance of primitive Long type. 
   */
  public static final LongType longType = new LongType();

  // Internal name of runtime support class for Long variants
  private static final String LONG_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/LongVariant";

  // Internal name of runtime support class for Long reference parameters
  private static final String LONG_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/LongReferenceParameter";

  /*
   * Creates a new Long type.
   */
  private LongType() {
    super(TypeKind.LONG);
  }

  @Override
  public boolean isLongType() {
    return true;
  }

  @Override
  public int bitsize() {
    return 64;
  }

  @Override
  public boolean isWideType() {
    return true;
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrLdc2(0L);
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
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "long2boolean", "(J)Z");
        break;
  
      case BYTE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "long2byte", "(J)B");
        break;
  
      case SHORT:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "long2short", "(J)S");
        break;
  
      case INTEGER:
        m.generateInstrL2i();
        break;
  
      case LONG:
        break;
  
      case SINGLE:
        m.generateInstrL2f();
        break;
  
      case DOUBLE:
        m.generateInstrL2d();
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(LONG_VARIANT_INTERNAL_NAME, "getLongVariant",
            "(J)L" + LONG_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokestatic("java/lang/Long", "toString", "(J)Ljava/lang/String;");
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrLload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrLstore(varIndex);
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrLaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrLastore();
  }

  @Override
  public void generateConst1(Method m) {
    m.generateInstrLdc2(1L);
  }

  @Override
  public void generateConstM1(Method m) {
    m.generateInstrLdc2(-1L);
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrLreturn();
  }

  @Override
  public void generatePop(Method m) {
    m.generateInstrPop2();
  }

  @Override
  public void generateNegation(Method m) {
    m.generateInstrLneg();
  }

  @Override
  public void generateAddition(Method m) {
    m.generateInstrLadd();
  }

  @Override
  public void generateSubtraction(Method m) {
    m.generateInstrLsub();
  }

  @Override
  public void generateMultiplication(Method m) {
    m.generateInstrLmul();
  }

  @Override
  public void generateIntegerDivision(Method m) {
    m.generateInstrLdiv();
  }

  @Override
  public void generateModulo(Method m) {
    m.generateInstrLrem();
  }

  @Override
  public void generateExponentiation(Method m) {
    m.generateInstrInvokestatic(EXPRESSION_HELPERS_INTERNAL_NAME, "pow", "(JJ)J");
  }

  @Override
  public void generateBitNot(Method m) {
    generateConstM1(m);
    m.generateInstrLxor();
  }

  @Override
  public void generateBitAnd(Method m) {
    m.generateInstrLand();
  }

  @Override
  public void generateBitOr(Method m) {
    m.generateInstrLor();
  }

  @Override
  public void generateBitXor(Method m) {
    m.generateInstrLxor();
  }

  @Override
  public void generateShiftLeft(Method m) {
    m.generateInstrLshl();
  }

  @Override
  public void generateShiftRight(Method m) {
    m.generateInstrLushr();
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrLcmp();
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrLcmp();
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrLcmp();
    m.generateInstrIflt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrLcmp();
    m.generateInstrIfle(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrLcmp();
    m.generateInstrIfgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrLcmp();
    m.generateInstrIfge(label);
  }

  @Override
  public void generateJavaBox(Method m) {
    m.generateInstrInvokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
  }

  @Override
  public String internalName() {
    return "J";
  }

  @Override
  public String referenceInternalName() {
    return LONG_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "J";
  }

  @Override
  public String signature() {
    return internalName();
  }

  @Override
  public String toString() {
    return "Long";
  }
}
