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
import com.google.devtools.simple.util.Strings;

import java.util.BitSet;
import java.util.List;

/**
 * Represents function types.
 * 
 * @author Herbert Czymontek
 */
public class FunctionType extends Type {

  // Indicates whether the function is an instance or object function
  private final boolean isInstanceFunction;

  // Formal argument list
  private final List<Type> formalArgumentTypes;

  // Bits indicating which parameters are reference parameters
  private final BitSet referenceParameters;
  
  // Result type of function (or null for procedures)
  private Type resultType;

  /**
   * Creates a new function type
   * 
   * @param isInstanceFunction  indicates whether this is an instance or object
   *                            function
   * @param formalArgumentTypes  formal argument type list
   * @param referenceParameters  bits indicating which parameters are reference
   *                             parameters (must not be {@code null}
   * @param resultType  function result type (or {@code null} for procedures)
   */
  public FunctionType(boolean isInstanceFunction, List<Type> formalArgumentTypes,
      BitSet referenceParameters,  Type resultType) {
    super(TypeKind.FUNCTION);

    this.isInstanceFunction = isInstanceFunction;
    this.formalArgumentTypes = formalArgumentTypes;
    this.referenceParameters = referenceParameters;
    this.resultType = resultType;
  }

  @Override
  public boolean canConvertTo(Type toType) {
    return false;
  }

  /**
   * Indicates whether the function is an instance or object function.
   * 
   * @return  {@code true} for instance functions, {@code false} otherwise
   */
  public boolean isInstanceFunction() {
    return isInstanceFunction;
  }

  /**
   * Returns the list of formal argument types.
   * 
   * @return  formal argument type list
   */
  public List<Type> getFormalArgumentList() {
    return formalArgumentTypes;
  }

  /**
   * Returns a bit set corresponding to parameter positions indicating
   * reference parameters.
   * 
   * @return  bit set indication reference parameters
   */
  public BitSet getReferenceParameter() {
    return referenceParameters;
  }
  
  /**
   * Returns the function result type.
   * 
   * @return  function result type or {@code null} for procedures
   */
  public Type getResultType() {
    return resultType;
  }

  @Override
  public boolean isFunctionType() {
    return true;
  }

  @Override
  protected boolean typeEquals(Type type) {
    if (!(type instanceof FunctionType)) {
      return false;
    }

    FunctionType functionType = (FunctionType) type;
    if (isInstanceFunction != functionType.isInstanceFunction) {
      return false;
    }

    if (resultType != null && functionType.resultType != null) {
      if (!resultType.equals(functionType.resultType)) {
        return false;
      }
    } else if (resultType != functionType.resultType) {
      return false;
    }

    int count = formalArgumentTypes.size();
    if (count != functionType.formalArgumentTypes.size()) {
      return false;
    }

    for (int i = 0; i < count; i++) {
      if (!formalArgumentTypes.get(i).equals(functionType.formalArgumentTypes.get(i))) {
        return false;
      }
    }

    return true;
  }
  
  @Override
  public void resolve(Compiler compiler) {
    for (Type argType : formalArgumentTypes) {
      argType.resolve(compiler);
    }

    if (resultType != null) {
      resultType.resolve(compiler);
    }
  }

  @Override
  public String internalName() {
    // COV_NF_START
    Compiler.internalError();
    return "";
    // COV_NF_END
  }

  @Override
  public String referenceInternalName() {
    // COV_NF_START
    Compiler.internalError();
    return "";
    // COV_NF_END
  }

  @Override
  public String referenceValueSignature() {
    // COV_NF_START
    Compiler.internalError();
    return "";
    // COV_NF_END
  }

  @Override
  public String signature() {
    StringBuilder sb = new StringBuilder("(");
    int formalArgCount = formalArgumentTypes.size();
    for (int i = 0; i < formalArgCount; i++) {
      Type t = formalArgumentTypes.get(i);
      sb.append(referenceParameters.get(i) ?
          'L' + t.referenceInternalName() + ';' :
          t.signature());
    }
    sb.append(')');
    sb.append(resultType == null ? "V" : resultType.signature());

    return sb.toString();
  }

  @Override
  public String toString() {
    // COV_NF_START
    return (resultType == null ? "Sub (" : "Function (") + Strings.join(", ", formalArgumentTypes) +
        ')'; 
    // COV_NF_END
  }
}
