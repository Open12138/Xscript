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

package com.google.devtools.simple.compiler.symbols;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.classfiles.Method.Label;
import com.google.devtools.simple.compiler.statements.synthetic.MarkerStatement;
import com.google.devtools.simple.compiler.types.ObjectType;

import java.util.List;

/**
 * Symbol used for local variables.
 * 
 * @author Herbert Czymontek
 */
public final class LocalVariableSymbol extends VariableSymbol {

  // Local variable index (according to the Java class file definition of local variable indices)
  private short varIndex;

  // Marks the scope of the variable (used for debug information generation)
  private MarkerStatement beginScope;
  private MarkerStatement endScope;

  // Indicates whether this is a reference parameter
  private final boolean isReferenceParameter;

  // Local temp for reference parameters
  private LocalVariableSymbol deRefTemp;

  /**
   * Creates a new local variable symbol.
   * 
   * @param position  source code start position of symbol 
   * @param name  local variable name
   * @param type  local variable type
   * @param varIndex  local variable index
   * @param isReferenceParameter  indicates whether this is a reference
   *                              parameter
   */
  public LocalVariableSymbol(long position, String name, Type type, short varIndex,
                             boolean isReferenceParameter) {
    super(position, name, type);

    this.varIndex = varIndex;
    this.isReferenceParameter = isReferenceParameter;
  }

  /**
   * Marks the start of the scope of the variable.
   *  
   * @param beginScope  start of scope marker
   */
  public void setBeginScope(MarkerStatement beginScope) {
    this.beginScope = beginScope;
  }

  /**
   * Marks the end of the scope of the variable.
   *  
   * @param endScope  end of scope marker
   */
  public void setEndScope(MarkerStatement endScope) {
    this.endScope = endScope;
  }

  /**
   * Returns the local variable index.
   * 
   * @return local variable index
   */
  public short getVarIndex() {
    return varIndex;
  }

  /**
   * Returns the actual local variable index (meaning for reference variables
   * the variable index of the local temp will be returned).
   * 
   * @return local variable index
   */
  public short getActualVarIndex() {
    return isReferenceParameter ? deRefTemp.varIndex : varIndex;
  }

  /**
   * Returns a label marking the start of the variable scope.
   * 
   * @return  start of variable scope
   */
  public Label getStartScopeLabel() {
    return beginScope.getMarker();
  }

  /**
   * Returns a label marking the end of the variable scope.
   * 
   * @return  end of variable scope
   */
  public Label getEndScopeLabel() {
    return endScope.getMarker();
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    // We will copy reference parameters into a local upon function entry and copy them back
    // upon function exit.
    if (isReferenceParameter) {
      deRefTemp = currentFunction.addTempVariable(getType());
    }
  }

  /**
   * Reads the value of a reference parameter and writes it into its associated
   * local temporary.
   *
   * @param m  method to generate the code for
   */
  public void readReferenceParameter(Method m) {
    if (isReferenceParameter) {
      ObjectType.objectType.generateLoadLocal(m, this);
      Type type = getType();
      type.generateLoadReferenceParameter(m);
      type.generateStoreLocal(m, deRefTemp);
    }
  }

  /**
   * Writes back the value of a reference parameter from its associated local
   * temporary.
   *
   * @param m  method to generate the code for
   */
  public void writeBackReferenceParameter(Method m) {
    if (isReferenceParameter) {
      ObjectType.objectType.generateLoadLocal(m, this);
      Type type = getType();
      type.generateLoadLocal(m, deRefTemp);
      type.generateStoreReferenceParameter(m);
    }
  }

  @Override
  public void generateRead(Method m) {
    getType().generateLoadLocal(m, isReferenceParameter ? deRefTemp : this);
  }

  @Override
  public void generateWrite(Method m) {
    getType().generateStoreLocal(m, isReferenceParameter ? deRefTemp : this);
  }

  @Override
  public void generateInitializer(Method m, List<Expression> dimensions) {
    // Need to explicitly initialize all local variables to appease the Java verifier
    // TODO: generate better code - limit to only those locals that aren't initialized explicitly
    if (dimensions != null) {
      for (Expression dimension : dimensions) {
        dimension.generate(m);
      }
      getType().generateAllocateArray(m);
    } else {
      getType().generateDefaultInitializationValue(m);
    }
    generateWrite(m);
  }

  @Override
  public String toString() {
    return "Dim " + getName() + " As " + getType().toString();  // COV_NF_LINE
  }
}
