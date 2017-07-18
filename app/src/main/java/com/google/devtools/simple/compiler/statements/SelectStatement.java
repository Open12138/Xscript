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

import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.classfiles.Method.Label;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.compiler.types.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Select-statement.
 * 
 * @author Herbert Czymontek
 */
public final class SelectStatement extends Statement {

  /**
   * Superclass for all Case statements. 
   */
  public final class SelectCaseStatement extends Statement {

    // List of Case expressions
    List<CaseExpression> caseExpressions;

    // Statements for this Case block
    private StatementBlock statements;

    /**
     * Creates a new Case statement.
     * 
     * @param position  source code start position of statement
     * @param statements  statements in case body
     */
    private SelectCaseStatement(long position, StatementBlock statements) {
      super(position);

      this.statements = statements;

      caseExpressions = new ArrayList<CaseExpression>();
    }

    /**
     * Adds a new range expression to the Case statement.
     * 
     * @param lowerBound  lower bound to compare selector expression against 
     * @param upperBound  upper bound to compare selector expression against
     */
    public void addRangeExpression(Expression lowerBound, Expression upperBound) {
      caseExpressions.add(new CaseRangeExpression(lowerBound, upperBound));
    }

    /**
     * Adds a new equal comparison expression to the Case statement.
     * 
     * @param expression  expression to compare selector expression with
     */
    public void addEqualExpression(Expression expression) {
      caseExpressions.add(new CaseEqualExpression(expression));
    }

    /**
     * Adds a new not-equal comparison expression to the Case statement.
     * 
     * @param expression  expression to compare selector expression with
     */
    public void addNotEqualExpression(Expression expression) {
      caseExpressions.add(new CaseNotEqualExpression(expression));
    }

    /**
     * Adds a new less comparison expression to the Case statement.
     * 
     * @param expression  expression to compare selector expression with
     */
    public void addLessExpression(Expression expression) {
      caseExpressions.add(new CaseLessExpression(expression));
    }

    /**
     * Adds a new less-or-equal comparison expression to the Case statement.
     * 
     * @param expression  expression to compare selector expression with
     */
    public void addLessOrEqualExpression(Expression expression) {
      caseExpressions.add(new CaseLessOrEqualExpression(expression));
    }

    /**
     * Adds a new greater comparison expression to the Case statement.
     * 
     * @param expression  expression to compare selector expression with
     */
    public void addGreaterExpression(Expression expression) {
      caseExpressions.add(new CaseGreaterExpression(expression));
    }

    /**
     * Adds a new greater-or-equal comparison expression to the Case statement.
     * 
     * @param expression  expression to compare selector expression with
     */
    public void addGreaterOrEqualExpression(Expression expression) {
      caseExpressions.add(new CaseGreaterOrEqualExpression(expression));
    }

    @Override
    public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
      statements.resolve(compiler, currentFunction, SelectStatement.this);
      
      for (CaseExpression caseExpression : caseExpressions) {
        caseExpression.resolve(compiler, currentFunction);
      }
    }

    @Override
    public void generate(Method m) {
      generateLineNumberInformation(m);
      Label nextStatementLabel = Method.newLabel();
      Label caseBodyLabel = Method.newLabel();

      int lastCaseExpressionIndex = caseExpressions.size() - 1;
      if (lastCaseExpressionIndex >= 0) {
        for (int i = 0; i < lastCaseExpressionIndex; i++) {
          caseExpressions.get(i).generateBranchToCaseBody(m, caseBodyLabel);
        }

        caseExpressions.get(lastCaseExpressionIndex).generateBranchToNextStatement(m, 
            nextStatementLabel);
      }

      m.setLabel(caseBodyLabel);
      statements.generate(m);
      m.generateInstrGoto(exitLabel);
      m.setLabel(nextStatementLabel);
    }

    /*
     * Indicates whether the statement is a Case-Else statement.
     */
    private boolean isCaseElseStatement() {
      return caseExpressions.isEmpty();
    }
  }

  /*
   * Superclass for all Case expressions
   */
  private abstract class CaseExpression {
    abstract void resolve(Compiler compiler, FunctionSymbol currentFunction);

    abstract void generateBranchToCaseBody(Method m, Label label);
    abstract void generateBranchToNextStatement(Method m, Label label);
  }

  /*
   * Superclass for all Case comparison expressions. 
   */
  private abstract class CaseComparisonExpression extends CaseExpression {
    // Expression to compare selector with
    protected Expression expression; 

    /**
     * Creates a new comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseComparisonExpression(Expression expression) {
      this.expression = expression;
    }

    @Override
    void resolve(Compiler compiler, FunctionSymbol currentFunction) {
      expression = expression.resolve(compiler, currentFunction).checkType(compiler,
          selector.getType());
    }

    void generate(Method m) {
      selector.getType().generateLoadLocal(m, selectorExprTemp);
      expression.generate(m);
    }
  }

  /*
   * Range expression for Case statements as in 'Case lower To upper'.
   */
  private final class CaseRangeExpression extends CaseExpression {
    // Range bounds to compare selector against
    private Expression lowerBound;
    private Expression upperBound;

    /**
     * Creates a new Case expression with a range bounds to compare the selector
     * against.
     * 
     * @param lowerBound  lower bound to compare selector against
     * @param upperBound  upper bound to compare selector against
     */
    CaseRangeExpression(Expression lowerBound, Expression upperBound) {
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
    }

    @Override
    public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
      Type selectorType = selector.getType();
      lowerBound = lowerBound.resolve(compiler, currentFunction).checkType(compiler, selectorType);
      upperBound = upperBound.resolve(compiler, currentFunction).checkType(compiler, selectorType);
      if (!selectorType.isScalarType()) {
        compiler.error(lowerBound.getPosition(), Error.errScalarTypeNeededInSelectCase);
      }
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      Label continueLabel = Method.newLabel();

      Type type = selector.getType();
      type.generateLoadLocal(m, selectorExprTemp);
      lowerBound.generate(m);
      type.generateBranchIfCmpLess(m, continueLabel);

      type.generateLoadLocal(m, selectorExprTemp);
      upperBound.generate(m);
      type.generateBranchIfCmpLessOrEqual(m, label);

      m.setLabel(continueLabel);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      Type type = selector.getType();
      type.generateLoadLocal(m, selectorExprTemp);
      lowerBound.generate(m);
      type.generateBranchIfCmpLess(m, label);

      type.generateLoadLocal(m, selectorExprTemp);
      upperBound.generate(m);
      type.generateBranchIfCmpGreater(m, label);      
    }
  }

  /*
   * Equal comparison expression for Case statements as in 'Case Is = expr'.
   */
  private final class CaseEqualExpression extends CaseComparisonExpression {
    /**
     * Creates a new equal comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseEqualExpression(Expression expression) {
      super(expression);
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpEqual(m, label);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpNotEqual(m, label);
    }
  }

  /*
   * Not-equal comparison expression for Case statements as in 'Case Is <> expr'.
   */
  private final class CaseNotEqualExpression extends CaseComparisonExpression {
    /**
     * Creates a new not-equal comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseNotEqualExpression(Expression expression) {
      super(expression);
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpNotEqual(m, label);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpEqual(m, label);
    }
  }

  /*
   * Ordered comparison expression for Case statements as in 'Case Is < expr' et al.
   */
  private abstract class CaseOrderedComparisionExpression extends CaseComparisonExpression {
    /**
     * Creates a new ordered comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseOrderedComparisionExpression(Expression expression) {
      super(expression);
    }

    @Override
    void resolve(Compiler compiler, FunctionSymbol currentFunction) {
      super.resolve(compiler, currentFunction);

      if (!selector.getType().isScalarType()) {
        compiler.error(expression.getPosition(), Error.errScalarTypeNeededInSelectCase);
      }
    }
  }

  /*
   * Less comparison expression for Case statements as in 'Case Is < expr'.
   */
  private final class CaseLessExpression extends CaseOrderedComparisionExpression {
    /**
     * Creates a new less comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseLessExpression(Expression expression) {
      super(expression);
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpLess(m, label);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpGreaterOrEqual(m, label);
    }
  }
  
  /*
   * Less-or-equal comparison expression for Case statements as in 'Case Is <= expr'.
   */
  private final class CaseLessOrEqualExpression extends CaseOrderedComparisionExpression {
    /**
     * Creates a new less-or-equal comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseLessOrEqualExpression(Expression expression) {
      super(expression);
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpLessOrEqual(m, label);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpGreater(m, label);
    }
  }

  /*
   * Greater comparison expression for Case statements as in 'Case Is > expr'.
   */
  private final class CaseGreaterExpression extends CaseOrderedComparisionExpression {
    /**
     * Creates a new greater comparison expression for Case statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseGreaterExpression(Expression expression) {
      super(expression);
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpGreater(m, label);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpLessOrEqual(m, label);
    }
  }

  /*
   * Greater-or-equal comparison expression for Case statements as in 'Case Is >= expr'.
   */
  private final class CaseGreaterOrEqualExpression extends CaseOrderedComparisionExpression {
    /**
     * Creates a new greater-or-equal comparison expression for Case
     * statements.
     * 
     * @param expression  expression to compare selector with
     */
    CaseGreaterOrEqualExpression(Expression expression) {
      super(expression);
    }

    @Override
    void generateBranchToCaseBody(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpGreaterOrEqual(m, label);
    }

    @Override
    void generateBranchToNextStatement(Method m, Label label) {
      generate(m);
      selector.getType().generateBranchIfCmpLess(m, label);
    }
  }

  // Selector expression
  private Expression selector;

  // Temporary variables to hold results of evaluated selector
  private LocalVariableSymbol selectorExprTemp;

  // List of Case statements
  private List<SelectCaseStatement> caseStatements;

  // Exit label
  private Label exitLabel;

  /**
   * Creates a new Select statement.
   * 
   * @param position  source code start position of statement
   * @param selector  expression to switch over
   */
  public SelectStatement(long position, Expression selector) {
    super(position);

    this.selector = selector;
    caseStatements = new ArrayList<SelectCaseStatement>();
  }

  /**
   * Adds a new Case statement to the Select statement.
   * 
   * @param position  source code start position of statement
   * @param statements  statements labeled by this Case statement
   * @return  case statement
   */
  public SelectCaseStatement newCaseStatement(long position, StatementBlock statements) {
    SelectCaseStatement caseStatement = new SelectCaseStatement(position, statements);
    caseStatements.add(caseStatement);
    
    return caseStatement;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    selector = selector.resolve(compiler, currentFunction);
    selectorExprTemp = currentFunction.addTempVariable(selector.getType());

    int lastCaseStatementIndex = caseStatements.size() - 1;
    for (int i = 0; i <= lastCaseStatementIndex; i++) {
      SelectCaseStatement caseStatement = caseStatements.get(i);
      if (caseStatement.isCaseElseStatement() && i < lastCaseStatementIndex) {
        compiler.error(caseStatement.getPosition(), Error.errCaseElseNotLast);
      }
      caseStatement.resolve(compiler, currentFunction);
    }

    // TODO: warning if there are any Case overlaps
  }

  @Override
  public void generate(Method m) {
    // Generate the selector expression into a temp
    generateLineNumberInformation(m);
    selector.generate(m);
    selector.getType().generateStoreLocal(m, selectorExprTemp);

    exitLabel = Method.newLabel();

    // Generate Case statements and their bodies
    for (SelectCaseStatement caseStatement : caseStatements) {
      caseStatement.generate(m);
    }

    m.setLabel(exitLabel);

    // TODO: generate better code - use lookupswitch and tableswitch instructions
  }

  @Override
  public String toString() {
    return "Select " + selector.toString();  // COV_NF_LINE
  }
}
