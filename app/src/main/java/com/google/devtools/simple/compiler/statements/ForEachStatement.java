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

package com.google.devtools.simple.compiler.statements;

import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.compiler.types.ArrayType;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.compiler.types.VariantType;

/**
 * This class implements the For-Next-Statement that iterates over the members
 * of a collection or array.
 * 
 * @author Herbert Czymontek
 */
public final class ForEachStatement extends IterativeLoopStatement {

  // Internal name for runtime library support for Collections
  private static final String COLLECTION_INTERNALNAME =
      Compiler.RUNTIME_ROOT_INTERNAL + "/collections/Collection";

  // Runtime library package containing helper methods for statements. 
  private static final String STATEMENT_HELPERS_INTERNAL_NAME =
      Compiler.RUNTIME_ROOT_INTERNAL + "/helpers/StmtHelpers";

  // Temporary variables to hold results of evaluated initialization, current index and count
  // expressions
  private LocalVariableSymbol initExprTemp;
  private LocalVariableSymbol indexTemp;
  private LocalVariableSymbol countTemp;

  /**
   * Creates a new For-Each-statement.
   *
   * @param position  source code start position of statement
   * @param loopVarExpr  variable over which to iterate
   * @param initExpr  initialization expression for loop variable
   * @param loopStatements  statements in loop body
   */
  public ForEachStatement(long position, Expression loopVarExpr, Expression initExpr,
      StatementBlock loopStatements) {
    super(position, loopVarExpr, initExpr, loopStatements);
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    Type initType = initExpr.getType();
    if (!initType.isArrayType() && !initType.isVariantType() &&
        !initType.internalName().equals(COLLECTION_INTERNALNAME)) {
      compiler.error(loopVarExpr.getPosition(), Error.errArrayOrCollectionNeededInForEach);
    }

    initExprTemp = currentFunction.addTempVariable(initType);
    indexTemp = currentFunction.addTempVariable(IntegerType.integerType);
    countTemp = currentFunction.addTempVariable(IntegerType.integerType);
  }

  @Override
  protected TokenKind getLoopStartToken() {
    return TokenKind.TOK_FOR;
  }

  @Override
  public void generate(Method m) {
    Type initType = initExpr.getType();

    // Generate the initialization expression into a temp
    generateLineNumberInformation(m);
    initExpr.generate(m);
    m.generateInstrDup();
    initType.generateStoreLocal(m, initExprTemp);

    if (initType.isVariantType()) {
      m.generateInstrInvokestatic(STATEMENT_HELPERS_INTERNAL_NAME, "forEachCount",
          "(L" + VariantType.VARIANT_INTERNAL_NAME + ";)I");
    } else if (initType.isArrayType()) {
      m.generateInstrArraylength();
    } else {
      m.generateInstrInvokevirtual(COLLECTION_INTERNALNAME, "Count", "()I");
    }
    IntegerType.integerType.generateStoreLocal(m, countTemp);

    // Initialize index variable
    // TODO: generate better code
    m.generateInstrLdc(0);
    IntegerType.integerType.generateStoreLocal(m, indexTemp);

    // Generate the loop body.
    Method.Label testLabel = Method.newLabel();
    m.generateInstrGoto(testLabel);

    Method.Label loopLabel = Method.newLabel();
    m.setLabel(loopLabel);

    loopVarExpr.generatePrepareWrite(m);
    initType.generateLoadLocal(m, initExprTemp);
    IntegerType.integerType.generateLoadLocal(m, indexTemp);
    Type loopVarType = loopVarExpr.getType();
    if (initType.isArrayType()) {
      Type elementType = ((ArrayType) initType).getElementType();
      elementType.generateLoadArray(m);
      if (!loopVarType.equals(elementType)) {
        elementType.generateConversion(m, loopVarType);
      }
    } else {
      if (initType.isVariantType()) {
        m.generateInstrInvokestatic(STATEMENT_HELPERS_INTERNAL_NAME, "forEachItem",
            "(L" + VariantType.VARIANT_INTERNAL_NAME + ";I)" +
                "L" + VariantType.VARIANT_INTERNAL_NAME + ";");
      } else {
        m.generateInstrInvokevirtual(COLLECTION_INTERNALNAME, "Item",
          "(I)L" + VariantType.VARIANT_INTERNAL_NAME + ';');
      }
      if (!loopVarType.equals(VariantType.variantType)) {
        VariantType.variantType.generateConversion(m, loopVarType);
      }
    }

    loopVarExpr.generateWrite(m);

    loopStatements.generate(m);

    // Generate step
    generateLineNumberInformation(m);
    m.generateInstrIinc(indexTemp.getVarIndex(), (byte)1);

    // Generate test
    m.setLabel(testLabel);
    IntegerType.integerType.generateLoadLocal(m, indexTemp);
    IntegerType.integerType.generateLoadLocal(m, countTemp);
    IntegerType.integerType.generateBranchIfCmpLess(m, loopLabel);

    m.setLabel(exitLabel);
  }

  @Override
  public String toString() {
    return "For Each" + loopVarExpr.toString() + " In " + initExpr.toString();  // COV_NF_LINE
  }
}
