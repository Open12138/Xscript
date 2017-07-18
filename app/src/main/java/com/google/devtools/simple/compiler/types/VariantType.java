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
 * Represents the primitive Variant type.
 * 
 * @author Herbert Czymontek
 */
public final class VariantType extends Type {

  /**
   * Instance of primitive Variant type. 
   */
  public static final VariantType variantType = new VariantType();

  /**
   * Internal package name for variant runtime helper classes.
   */
  protected final static String VARIANT_PACKAGE = Compiler.RUNTIME_ROOT_INTERNAL + "/variants";

  public final static String VARIANT_INTERNAL_NAME = VARIANT_PACKAGE + "/Variant";
  public final static String UNINITIALIZED_VARIANT_INTERNAL_NAME =
      VARIANT_PACKAGE + "/UninitializedVariant";
  public final static String VARIANT_NAME = VARIANT_INTERNAL_NAME.replace('/', '.');

  private final static String VARIANT_BINOP_SIGNATURE =
      "(L" + VARIANT_INTERNAL_NAME + ";)L" + VARIANT_INTERNAL_NAME + ";";
  private final static String VARIANT_CMPOP_SIGNATURE = "(L" + VARIANT_INTERNAL_NAME + ";)I";
  private final static String VARIANT_IDENTICALOP_SIGNATURE = "(L" + VARIANT_INTERNAL_NAME + ";)Z";
  private final static String VARIANT_LIKEOP_SIGNATURE = "(L" + VARIANT_INTERNAL_NAME + ";)Z";
  private final static String VARIANT_TYPEOFOP_SIGNATURE = "(Ljava/lang/String;)Z";
  private final static String VARIANT_UNOP_SIGNATURE = "()L" + VARIANT_INTERNAL_NAME + ";";

  // Internal name of runtime support class for Variant reference parameters
  private static final String VARIANT_REFERENCE_INTERNAL_NAME =
      REFERENCE_PACKAGE_INTERNAL_NAME + "/VariantReferenceParameter";

  /*
   * Creates a new Variant type.
   */
  private VariantType() {
    super(TypeKind.VARIANT);
  }

  @Override
  public boolean isScalarIntegerType() {
    return true;
  }

  @Override
  public boolean isScalarType() {
    return true;
  }

  @Override
  public boolean isVariantType() {
    return true;
  }

  @Override
  public boolean canConvertTo(Type toType) {
    return true;
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrInvokestatic(UNINITIALIZED_VARIANT_INTERNAL_NAME, "getUninitializedVariant",
        "()L" + UNINITIALIZED_VARIANT_INTERNAL_NAME + ";");
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
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getBoolean", "()Z");
        break;
  
      case BYTE:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getByte", "()B");
        break;
  
      case DOUBLE:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getDouble", "()D");
        break;
  
      case INTEGER:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getInteger", "()I");
        break;
  
      case LONG:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getLong", "()J");
        break;
  
      case SHORT:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getShort", "()S");
        break;
  
      case SINGLE:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getSingle", "()F");
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case OBJECT:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getObject", "()Ljava/lang/Object;");
        break;
  
      case ARRAY:
        m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getArray", "()Ljava/lang/Object;");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "getString", "()Ljava/lang/String;");
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
  public void generateNegation(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "neg", VARIANT_UNOP_SIGNATURE);
  }

  @Override
  public void generateAddition(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "add", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateSubtraction(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "sub", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateMultiplication(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "mul", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateDivision(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "div", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateIntegerDivision(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "idiv", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateModulo(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "mod", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateExponentiation(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "pow", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateBitNot(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "not", VARIANT_UNOP_SIGNATURE);
  }

  @Override
  public void generateBitAnd(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "and", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateBitOr(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "or", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateBitXor(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "xor", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateShiftLeft(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "shl", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateShiftRight(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "shr", VARIANT_BINOP_SIGNATURE);
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "cmp", VARIANT_CMPOP_SIGNATURE);
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "cmp", VARIANT_CMPOP_SIGNATURE);
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "cmp", VARIANT_CMPOP_SIGNATURE);
    m.generateInstrIflt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "cmp", VARIANT_CMPOP_SIGNATURE);
    m.generateInstrIfle(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "cmp", VARIANT_CMPOP_SIGNATURE);
    m.generateInstrIfgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "cmp", VARIANT_CMPOP_SIGNATURE);
    m.generateInstrIfge(label);
  }

  @Override
  public void generateBranchIfCmpIdentical(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "identical", VARIANT_IDENTICALOP_SIGNATURE);
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpNotIdentical(Method m, Method.Label label) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "identical", VARIANT_IDENTICALOP_SIGNATURE);
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateTypeOf(Method m, String internalName) {
    m.generateInstrLdc(internalName);
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "typeof", VARIANT_TYPEOFOP_SIGNATURE);
  }

  @Override
  public void generateLike(Method m) {
    m.generateInstrInvokevirtual(VARIANT_INTERNAL_NAME, "like", VARIANT_LIKEOP_SIGNATURE);
  }

  @Override
  public String internalName() {
    return VARIANT_INTERNAL_NAME;
  }

  @Override
  public String referenceInternalName() {
    return VARIANT_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return signature();
  }

  @Override
  public String signature() {
    return "L" + VARIANT_INTERNAL_NAME + ";";
  }

  @Override
  public String toString() {
    return "Variant";
  }
}
