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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.expressions.IdentifierExpression;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.classfiles.Method.Label;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.Symbol;
import com.google.devtools.simple.compiler.types.ObjectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the On Error statement.
 * 
 * @author Herbert Czymontek
 */
public final class OnErrorStatement extends Statement {

  /**
   * Implements Case statements within the On Error statement. 
   */
  public final class OnErrorCaseStatement extends Statement {

    // List of Case type expressions
    List<Expression> caseExpressions;

    // Statements for this Case block
    private StatementBlock statements;

    /**
     * Creates a new Case statement.
     * 
     * @param position  source code start position of statement
     * @param statements  statements in case body
     */
    private OnErrorCaseStatement(long position, StatementBlock statements) {
      super(position);

      this.statements = statements;

      caseExpressions = new ArrayList<Expression>();
    }

    /**
     * Adds a new type expression to the Case statement.
     * 
     * @param expression  type expression
     */
    public void addTypeExpression(Expression expression) {
      caseExpressions.add(expression);
    }

    @Override
    public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
      statements.resolve(compiler, currentFunction, OnErrorStatement.this);

      for (int i = 0; i < caseExpressions.size(); i++) {
        Expression caseExpression = caseExpressions.get(i).resolve(compiler, currentFunction);
        if (!isRuntimeErrorTypeName(compiler, caseExpression)) {
          compiler.error(caseExpression.getPosition(), Error.errRuntimeErrorTypeNeeded);
        }
        caseExpressions.set(i, caseExpression);
      }
    }

    @Override
    public void generate(Method m) {
      generateLineNumberInformation(m);

      Label nextStatementLabel = Method.newLabel();
      Label caseBodyLabel = Method.newLabel();

      int lastCaseExpressionIndex = caseExpressions.size() - 1;
      if (lastCaseExpressionIndex >= 0) {
        // If you have a list of n conditions followed by statements to be executed when one of them
        // is true, then 1 to n-1 need to jump on true to the statements while n needs to jump on
        // false past the statements.
        for (int i = 0; i < lastCaseExpressionIndex; i++) {
          runtimeErrorTemp.generateRead(m);
          m.generateInstrInstanceof(caseExpressions.get(i).getType().internalName());
          m.generateInstrIfne(caseBodyLabel);
        }

        runtimeErrorTemp.generateRead(m);
        m.generateInstrInstanceof(
            caseExpressions.get(lastCaseExpressionIndex).getType().internalName());
        m.generateInstrIfeq(nextStatementLabel);
      }

      m.setLabel(caseBodyLabel);
      statements.generate(m);
      m.generateInstrGoto(exitLabel);

      m.setLabel(nextStatementLabel);
    }

    /*
     * Checks whether the given expression represents a runtime error type name.
     */
    private boolean isRuntimeErrorTypeName(Compiler compiler, Expression expression) {
      if (!(expression instanceof IdentifierExpression)) {
        return false;
      }

      Symbol identifier = ((IdentifierExpression) expression).getResolvedIdentifier();
      if (!(identifier instanceof ObjectSymbol)) {
        return false;
      }

      return ((ObjectType) ((ObjectSymbol) identifier).getType()).isBaseObject(
          compiler.getRuntimeErrorType());
    }

    /*
     * Indicates whether the statement is a Case-Else statement.
     */
    private boolean isCaseElseStatement() {
      return caseExpressions.isEmpty();
    }
  }

  // List of Case statements
  private List<OnErrorCaseStatement> caseStatements;

  // Function exit label
  private Label exitLabel;

  // Temporary variables to hold results of evaluated selector
  private LocalVariableSymbol runtimeErrorTemp;

  /**
   * Creates a new On Error statement.
   * 
   * @param position  source code start position of statement
   */
  public OnErrorStatement(long position) {
    super(position);

    caseStatements = new ArrayList<OnErrorCaseStatement>();
  }

  /**
   * Adds a new Case statement to the On Error statement.
   * 
   * @param position  source code start position of statement
   * @param statements  statements labeled by this Case statement
   * @return  case statement
   */
  public OnErrorCaseStatement newCaseStatement(long position, StatementBlock statements) {
    OnErrorCaseStatement caseStatement = new OnErrorCaseStatement(position, statements);
    caseStatements.add(caseStatement);

    return caseStatement;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {

    int lastCaseStatementIndex = caseStatements.size() - 1;
    for (int i = 0; i <= lastCaseStatementIndex; i++) {
      OnErrorCaseStatement caseStatement = caseStatements.get(i);
      if (caseStatement.isCaseElseStatement() && i < lastCaseStatementIndex) {
        compiler.error(caseStatement.getPosition(), Error.errCaseElseNotLast);
      }
      caseStatement.resolve(compiler, currentFunction);
    }

    runtimeErrorTemp = currentFunction.addTempVariable(ObjectType.objectType);

    exitLabel = currentFunction.getExitLabel();
  }

  @Override
  public void generate(Method m) {
    // Call helper method that will convert any Java Exceptions into their Simple runtime error
    // equivalent
    m.generateInstrInvokestatic(Compiler.RUNTIME_ERROR_INTERNAL_NAME, "convertToRuntimeError",
        "(Ljava/lang/Throwable;)L" + Compiler.RUNTIME_ERROR_INTERNAL_NAME + ';');

    // Store in temp variable
    runtimeErrorTemp.generateWrite(m);

    // Generate Case statements and their bodies
    for (OnErrorCaseStatement caseStatement : caseStatements) {
      caseStatement.generate(m);
    }

    // In case no matching handler was found we simply pass down the runtime error (which in Java
    // terms means rethrowing it)
    runtimeErrorTemp.generateRead(m);
    m.generateInstrAthrow();
  }

  @Override
  public String toString() {
    return "On Error";  // COV_NF_LINE
  }
}
