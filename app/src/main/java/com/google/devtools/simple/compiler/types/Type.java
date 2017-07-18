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
import com.google.devtools.simple.compiler.expressions.LikeExpression;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.util.Preconditions;

/**
 * Superclass for all type.
 * 
 * @author Herbert Czymontek
 */
public abstract class Type {

  /**
   * Runtime library package containing helper methods for type conversions. 
   */
  protected static final String CONVERSION_HELPERS_INTERNAL_NAME =
      Compiler.RUNTIME_ROOT_INTERNAL + "/helpers/ConvHelpers";

  /**
   * Runtime library package containing helper methods for expressions. 
   */
  protected static final String EXPRESSION_HELPERS_INTERNAL_NAME =
      Compiler.RUNTIME_ROOT_INTERNAL + "/helpers/ExprHelpers";

  /**
   * Runtime library package containing helper methods for reference parameters. 
   */
  protected static final String REFERENCE_PACKAGE_INTERNAL_NAME =
      Compiler.RUNTIME_ROOT_INTERNAL + "/parameters";
  
  
  /**
   * Type kinds should only be used in switch statements!
   */
  protected enum TypeKind {
    ARRAY,
    BOOLEAN,
    BYTE,
    DATE,
    DOUBLE,
    FUNCTION,
    INTEGER,
    LONG,
    OBJECT,
    SHORT,
    SINGLE,
    STRING,
    VARIANT,
    
    ERROR;
  }

  // Type kind
  private final TypeKind kind;

  /**
   * Creates a new type.
   * 
   * @param kind  type kind
   */
  public Type(TypeKind kind) {
    this.kind = kind;
  }

  /**
   * Returns the kind of the type.
   * 
   * @return  type kind
   */
  protected final TypeKind getKind() {
    return kind;
  }

  @Override
  public boolean equals(Object obj) {
    Preconditions.checkArgument(obj instanceof Type);

    Type type = ((Type) obj).getType();
    if (type.isErrorType()) {
      return true;
    }
    
    return getType().typeEquals(type);
  }

  @Override
  public int hashCode() {
    return getType().hashCode();
  }

  /**
   * Checks whether two types are equal. Subclasses (other than primitive
   * types) need to override this method.
   * 
   * @param type  type to compare against
   * @return  {@code true} if types are equal, {@code false} otherwise
   */
  protected boolean typeEquals(Type type) {
    // Note: I really want '=='
    return this == type;
  }

  /**
   * Returns the resolved type.
   * 
   * @return  resolved type
   */
  public Type getType() {
    return this;
  }

  /**
   * Indicates whether a type is a wide type (like Long and Double).
   * 
   * @return  {@code true} if the type is wide, {@code false} otherwise
   */
  public boolean isWideType() {
    return false;
  }

  /**
   * Checks whether the type is an array type.
   * 
   * @return  {@code true} for array types, {@code false} otherwise
   */
  public boolean isArrayType() {
    return false;
  }

  /**
   * Checks whether the type is a function type.
   * 
   * @return  {@code true} for function types, {@code false} otherwise
   */
  public boolean isFunctionType() {
    return false;
  }

  /**
   * Checks whether the type is an object type.
   * 
   * @return  {@code true} for object types, {@code false} otherwise
   */
  public boolean isObjectType() {
    return false;
  }

  /**
   * Checks whether the type is the Boolean type.
   * 
   * @return  {@code true} for Boolean type, {@code false} otherwise
   */
  public boolean isBooleanType() {
    return false;
  }

  /**
   * Checks whether the type is a scalar integer type (not to be confused with
   * the Integer type).
   * 
   * @return  {@code true} for scalar integer types, {@code false} otherwise
   */
  public boolean isScalarIntegerType() {
    return false;
  }

  /**
   * Checks whether the type is the Long type.
   * 
   * @return  {@code true} for Long type, {@code false} otherwise
   */
  public boolean isLongType() {
    return false;
  }

  /**
   * Checks whether the type is a scalar type.
   * 
   * @return  {@code true} for scalar types, {@code false} otherwise
   */
  public boolean isScalarType() {
    return false;
  }

  /**
   * Checks whether the type is an error type.
   * 
   * @return  {@code true} for error types, {@code false} otherwise
   */
  public boolean isErrorType() {
    return false;
  }

  /**
   * Checks whether the type is the Variant type.
   * 
   * @return  {@code true} for Variant type, {@code false} otherwise
   */
  public boolean isVariantType() {
    return false;
  }

  /**
   * Resolves a type.
   * 
   * @param compiler  current compiler instance
   */
  public void resolve(Compiler compiler) {
  }

  /**
   * Indicates whether this type can be converted into another. This method
   * assumes that the types being checked are different.
   *
   * @param toType  type to convert this type into
   * @return  {@code true} if the conversion can be done (even if only
   *          potentially), {@code false} otherwise
   */
  public abstract boolean canConvertTo(Type toType);

  /**
   * Generates byte code for the default initialization value for the type.
   * 
   * @param m  method to generate the code for
   */
  public void generateDefaultInitializationValue(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a runtime value conversion of one type to another.
   * 
   * @param m  method to generate the code for
   * @param toType  type to convert to
   */
  public void generateConversion(Method m, Type toType) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to convert a value into a string.
   * 
   * @param m  method to generate the code for
   */
  public void generateToString(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for loading a local variable.
   * 
   * @param m  method to generate the code for
   * @param local  local variable
   */
  public final void generateLoadLocal(Method m, LocalVariableSymbol local) {
    generateLoadLocal(m, local.getVarIndex());
  }

  /**
   * Generates byte code for loading a local variable.
   * 
   * @param m  method to generate the code for
   * @param varIndex  local variable index
   */
  public void generateLoadLocal(Method m, short varIndex) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for storing a local variable.
   * 
   * @param m  method to generate the code for
   * @param local  local variable
   */
  public final void generateStoreLocal(Method m, LocalVariableSymbol local) {
    generateStoreLocal(m, local.getVarIndex());
  }

  /**
   * Generates byte code for storing a local variable.
   * 
   * @param m  method to generate the code for
   * @param varIndex  local variable index
   */
  public void generateStoreLocal(Method m, short varIndex) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for loading a reference parameter.
   * 
   * @param m  method to generate the code for
   */
  public final void generateLoadReferenceParameter(Method m) {
    String valueSignature = referenceValueSignature();
    m.generateInstrInvokevirtual(referenceInternalName(), "get",
        "()" + valueSignature);
    if (valueSignature.equals("Ljava/lang/Object;")) {
      m.generateInstrCheckcast(internalName());
    }
  }

  /**
   * Generates byte code for storing a reference parameter.
   * 
   * @param m  method to generate the code for
   */
  public final void generateStoreReferenceParameter(Method m) {
    m.generateInstrInvokevirtual(referenceInternalName(), "set",
        '(' + referenceValueSignature() + ")V");
  }

  /**
   * Generates byte code for loading an array element.
   * 
   * @param m  method to generate the code for
   */
  public void generateLoadArray(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for storing an array element.
   * 
   * @param m  method to generate the code for
   */
  public void generateStoreArray(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for the constant 1.
   * 
   * @param m  method to generate the code for
   */
  public void generateConst1(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for the constant -1.
   * 
   * @param m  method to generate the code for
   */
  public void generateConstM1(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a return instruction.
   * 
   * @param m  method to generate the code for
   */
  public void generateReturn(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for popping a value of the type off the stack.
   * 
   * @param m  method to generate the code for
   */
  public void generatePop(Method m) {
    m.generateInstrPop();
  }

  /**
   * Generates byte code for a negation.
   * 
   * @param m  method to generate the code for
   */
  public void generateNegation(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for an addition.
   * 
   * @param m  method to generate the code for
   */
  public void generateAddition(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a subtraction.
   * 
   * @param m  method to generate the code for
   */
  public void generateSubtraction(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a multiplication.
   * 
   * @param m  method to generate the code for
   */
  public void generateMultiplication(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a division.
   * 
   * @param m  method to generate the code for
   */
  public void generateDivision(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for an integer division.
   * 
   * @param m  method to generate the code for
   */
  public void generateIntegerDivision(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a modulo operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateModulo(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for an exponentiation.
   * 
   * @param m  method to generate the code for
   */
  public void generateExponentiation(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a bit-wise NOT operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateBitNot(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a bit-wise AND operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateBitAnd(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a bit-wise OR operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateBitOr(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a bit-wise XOR operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateBitXor(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a shift left operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateShiftLeft(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a shift right operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateShiftRight(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of an equals
   * comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of a not
   * equals comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of less
   * comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of a less or
   * equal comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of a greater
   * comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of a greater
   * or equal comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of an
   * identical comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpIdentical(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code to branch to a label based on the result of a not
   * identical comparison.
   * 
   * @param m  method to generate the code for
   * @param label label to branch to
   */
  public void generateBranchIfCmpNotIdentical(Method m, Method.Label label) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a 'Like' comparison.
   * 
   * @see LikeExpression
   * 
   * @param m  method to generate the code for
   */
  public void generateLike(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for a type check operation.
   * 
   * @param m  method to generate the code for
   * @param internalName  internal name of type to check against
   */
  public void generateTypeOf(Method m, String internalName) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates byte code for array allocation.
   * 
   * @param m  method to generate the code for
   */
  public void generateAllocateArray(Method m) {
    Compiler.internalError();  // COV_NF_LINE    
  }

  /**
   * Boxes primitive Simple types into their boxed Java types.
   * Doesn't do anything for all other types.
   * 
   * @param m  method to generate the code for
   */
  public void generateJavaBox(Method m) {
  }
  
  /**
   * Returns the type's internal name.
   * 
   * @return  internal type name
   */
  public abstract String internalName();

  /**
   * Returns the internal name of the type's reference helper class.
   * 
   * @return  internal name of corresponding reference helper class
   */
  public abstract String referenceInternalName();

  /**
   * Returns the signature of the type's reference value.
   * 
   * @return  signature of corresponding reference value
   */
  public abstract String referenceValueSignature();

  /**
   * Returns the type's signature.
   * 
   * @return  type signature
   */
  public String signature() {
    return internalName();
  }
}
