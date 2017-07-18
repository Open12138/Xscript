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

package com.google.devtools.simple.compiler.expressions;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.FunctionType;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.compiler.types.VariantType;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.EventSymbol;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.compiler.types.ArrayType;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.util.Strings;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles call (function invocation) expression.
 * 
 * <p>Unfortunately, due to the established syntax for array access in various
 * BASIC dialects, array access expressions look exactly like call expressions.
 * Therefore we also need to be able to handle array access expressions.
 * 
 * @author Herbert Czymontek
 */
public final class CallExpression extends Expression implements ExpressionValueUnused {
  // Function access expression
  private Expression functionExpr;

  // List of actual arguments
  private final List<Expression> actualArgList;

  // Indicates whether the function result is used or not (if there is a result)
  private boolean resultUnused;

  // Indicates whether this is an array access expression (rather than a call expression)
  private boolean isArrayAccess;
  
  // Indicates that this is a variant array access
  private boolean isVariantArrayAccess;

  // Indicates that this is a variant invocation
  private boolean isVariantInvoke;

  // Maps actual arguments to their reference wrapper temporary variables (if the argument is not an
  // lvalue then the temp variable will be null)
  private final Map<Expression, LocalVariableSymbol> refTempMap;

  /**
   * Creates a new call expression.
   *
   * @param position  source code start position of expression
   * @param functionExpr  function access expression
   * @param actualArgList  list of actual arguments
   */
  public CallExpression(long position, Expression functionExpr, List<Expression> actualArgList) {
    super(position);

    this.functionExpr = functionExpr;
    this.actualArgList = actualArgList;

    refTempMap = new HashMap<Expression, LocalVariableSymbol>();
    resultUnused = false;
  }

  @Override
  public boolean isAssignable() {
    return isArrayAccess;
  }
  
  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Resolve function expression
    functionExpr = functionExpr.resolve(compiler, currentFunction);
    type = functionExpr.getType();

    // The moment of truth: is this a call expression or an array access? Or even a variant
    // array access or invocation?
    if (type == null || type.isVariantType()) {
      // In this case the call (or array access) will be resolved at runtime.
      if (type == null) {
        isVariantInvoke = true;

        if (!(functionExpr instanceof QualifiedIdentifierExpression)) {
          compiler.error(functionExpr.getPosition(), Error.errFunctionOrArrayTypeNeeded);
        }

        // The result of the call will always be a variant (will return null if it is a procedure
        // call).
        type = VariantType.variantType;

      } else {
        isVariantArrayAccess = true;
        isArrayAccess = true;
      }

      // Resolve arguments (all arguments need to be variants themselves)
      int actualArgCount = actualArgList.size();
      for (int i = 0; i < actualArgCount; i++) {
        Expression arg = actualArgList.get(i).resolve(compiler, currentFunction);
  
        // If the actual argument type is not exactly the formal argument type
        // then try to find a conversion from actual to formal argument type.
        actualArgList.set(i, arg.checkType(compiler, VariantType.variantType));
      }
    } else if (type.isArrayType()) {
      // Expression type is the array element type
      isArrayAccess = true;

      ArrayType arrayType = (ArrayType) type;
      type = arrayType.getElementType();

      // Make sure the number of indices matches the number of array dimensions
      int actualArgCount = actualArgList.size();
      int arrayDimensions = arrayType.getDimensions(); 
      if (arrayDimensions != actualArgCount) {
        compiler.error(getPosition(),
            actualArgCount > arrayDimensions ? Error.errTooManyIndices : Error.errTooFewIndices);
      }

      // Resolve indices 
      for (int i = 0; i < actualArgCount; i++) {
        Expression index = actualArgList.get(i).resolve(compiler, currentFunction);
        actualArgList.set(i, index.checkType(compiler, IntegerType.integerType));
      }

    } else if (type.isFunctionType()) {
      // Make sure this is not an attempt to invoke an event handler directly
      if (((IdentifierExpression) functionExpr).getResolvedIdentifier() instanceof EventSymbol) {
        compiler.error(functionExpr.getPosition(), Error.errCannotInvokeEvent);
      }

      // Expression type is the return type
      FunctionType funcType = (FunctionType) type;
      type = funcType.getResultType();
  
      // Check argument counts of formal and actual argument list
      List<Type> formalArgList = funcType.getFormalArgumentList();
  
      int actualArgCount = actualArgList.size();
      int formalArgCount = formalArgList.size();
      if (actualArgCount != formalArgCount) {
        compiler.error(getPosition(),
            actualArgCount > formalArgCount ? Error.errTooManyArguments : Error.errTooFewArguments);
      }

      // Resolve arguments
      BitSet referenceParameters = funcType.getReferenceParameter();
      for (int i = 0; i < actualArgCount; i++) {
        // If the actual argument type is not exactly the formal argument type
        // then try to find a conversion from actual to formal argument type.
        Expression arg = actualArgList.get(i).resolve(compiler, currentFunction);

        if (i < formalArgCount) {
          // We only do this so that we don't crash in the case of too many arguments
          arg = arg.checkType(compiler, formalArgList.get(i));
        }

        if (referenceParameters.get(i)) {
          refTempMap.put(arg, arg.isAssignable() ?
              currentFunction.addTempVariable(ObjectType.objectType) :
              null);
        }

        actualArgList.set(i, arg);
      }
    } else {
      compiler.error(functionExpr.getPosition(), Error.errFunctionOrArrayTypeNeeded);
    }

    return fold(compiler, currentFunction);
  }

  @Override
  public boolean isCallExpression() {
    if (isVariantInvoke) {
      return true;
    } else if (isVariantArrayAccess) {
      return false;
    } else if (functionExpr.getType().isArrayType()) {
      return false;
    } else {
      return true;
    }
  }

  /*
   * This method contains the part of code generation that is common to all the code generation
   * methods. 
   */
  private void generatePrepareArrayAccess(Method m) {
    // Generate array expression
    functionExpr.generate(m);

    // Multidimensional arrays are actually multidimensional Java arrays which are nested arrays.
    // Therefore we need to repeatedly load those inner arrays before we can access the array
    // element we are actually interested in.
    int lastIndex = actualArgList.size() - 1;
    for (int i = 0; i < lastIndex; i++) {
      Expression index = actualArgList.get(i);
      index.generate(m);
      m.generateInstrAaload();
    }

    // Generate index for the array element we are actually interested in
    Expression index = actualArgList.get(lastIndex);
    index.generate(m);
  }

  @Override
  public void generatePrepareWrite(Method m) {
    if (isVariantArrayAccess) {
      // Generate code for accessing an array variant's elements
      functionExpr.generate(m);

      // Generate indices into an array of variants
      generateArgumentsIntoVariantArray(m);
    } else {
      generatePrepareArrayAccess(m);
    }
  }

  @Override
  public void generateWrite(Method m) {
    if (isVariantArrayAccess) {
      // Generate variant accessor
      m.generateInstrInvokevirtual(VariantType.VARIANT_INTERNAL_NAME, "array",
          "([L" + VariantType.VARIANT_INTERNAL_NAME + ";L" + VariantType.VARIANT_INTERNAL_NAME +
              ";)V");
    } else {
      type.generateStoreArray(m);
    }
  }

  private void generateArgumentsIntoVariantArray(Method m) {
    int actualArgCount = actualArgList.size();
    m.generateInstrLdc(actualArgCount);
    m.generateInstrAnewarray(VariantType.VARIANT_INTERNAL_NAME);
  
    for (int i = 0; i < actualArgCount; i++) {
      m.generateInstrDup();
      m.generateInstrLdc(i);
      actualArgList.get(i).generate(m);
      m.generateInstrAastore();
    }
  }

  @Override
  public void generate(Method m) {
    if (isVariantInvoke) {
      // Generate code for function expression (like 'this' argument - or 'Me' in Simple)
      functionExpr.generatePrepareInvoke(m);
    
      // Generate function name
      IdentifierExpression identifier = (IdentifierExpression) functionExpr;
      m.generateInstrLdc(identifier.getResolvedIdentifier().getName());

      // Generate arguments into an array of variants
      generateArgumentsIntoVariantArray(m);

      // Generate variant invoker
      m.generateInstrInvokevirtual(VariantType.VARIANT_INTERNAL_NAME, "function",
          "(Ljava/lang/String;[L" + VariantType.VARIANT_INTERNAL_NAME + ";)L" +
              VariantType.VARIANT_INTERNAL_NAME + ";");
    
      // TODO: what should we do about reference parameters?
      
      // Discard result if it is not used
      if (resultUnused) {
        type.generatePop(m);
      }
    } else if (isVariantArrayAccess) {
      // Generate code for accessing an array variant's elements
      functionExpr.generate(m);

      // Generate indices into an array of variants
      generateArgumentsIntoVariantArray(m);

      // Generate variant invoker
      m.generateInstrInvokevirtual(VariantType.VARIANT_INTERNAL_NAME, "array",
          "([L" + VariantType.VARIANT_INTERNAL_NAME + ";)L" + VariantType.VARIANT_INTERNAL_NAME +
              ";");

    } else if (isArrayAccess) {
      // Generate code for array access
      generatePrepareArrayAccess(m);
      type.generateLoadArray(m);

    } else {
      // Generate code for function expression (like 'this' argument - or 'Me' in Simple)
      functionExpr.generatePrepareInvoke(m);
  
      // Generate actual arguments
      for (Expression arg : actualArgList) {
        if (refTempMap.containsKey(arg)) {
          Type argType = arg.getType();
          String referenceInternalName = argType.referenceInternalName(); 
          m.generateInstrNew(referenceInternalName);
          m.generateInstrDup();
          arg.generate(m);
          m.generateInstrInvokespecial(referenceInternalName, "<init>",
              '(' + argType.referenceValueSignature() + ")V");

          LocalVariableSymbol temp = refTempMap.get(arg);
          if (temp != null) {
            m.generateInstrDup();
            temp.generateWrite(m);
          }
        } else {
          arg.generate(m);
        }
      }

      // Generate invoke instruction
      functionExpr.generateInvoke(m);

      // Discard result if there is one and it is not used
      if (resultUnused && type != null) {
        type.generatePop(m);
      }

      // Any reference write-backs?
      if (!refTempMap.isEmpty()) {
        for (Expression arg : refTempMap.keySet()) {
          LocalVariableSymbol temp = refTempMap.get(arg);
          if (temp != null) {
            arg.generatePrepareWrite(m);
            temp.generateRead(m);
            arg.getType().generateLoadReferenceParameter(m);
            arg.generateWrite(m);
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    return functionExpr.toString() + '(' + Strings.join(", ", actualArgList) + ')';   // COV_NF_LINE
  }

  // ExpressionValueUnused implementation

  public void valueUnused(Compiler compiler) {
    resultUnused = true;
    
    if (isVariantArrayAccess) {
      compiler.error(getPosition(), Error.errAssignmentOrCallExprExpected);
    }
  }
}
