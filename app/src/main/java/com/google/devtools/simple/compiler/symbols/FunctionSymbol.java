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

import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.classfiles.ClassFile;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.classfiles.MethodTooBigException;
import com.google.devtools.simple.classfiles.Method.Label;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.scopes.LocalScope;
import com.google.devtools.simple.compiler.RuntimeLoader;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.statements.OnErrorStatement;
import com.google.devtools.simple.compiler.statements.StatementBlock;
import com.google.devtools.simple.compiler.statements.synthetic.LocalVariableDefinitionStatement;
import com.google.devtools.simple.compiler.statements.synthetic.MarkerStatement;
import com.google.devtools.simple.compiler.types.FunctionType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.util.Preconditions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Superclass for all function symbols.
 *
 * @author Herbert Czymontek
 */
public abstract class FunctionSymbol extends Symbol implements SymbolWithType {

  /*
   * This is a local helper class used to store information that available for compiled functions
   * only. Separating this information results in smaller function symbols for functions defined
   * from loaded class files.
   */
  private class CompilationInformation {
    // Number of variables used by function
    private short varCount;

    // List of formal parameters
    private final List<LocalVariableSymbol> params;

    // List of local variables
    private final List<LocalVariableSymbol> locals;

    // Function scope
    private final LocalScope scope;

    // 'Me' variable - can be null for static functions
    private LocalVariableSymbol me;

    // Local variable for function results (will be null for procedures)
    private LocalVariableSymbol returnVariable;

    // Statements in function body (can be null for interface function definitions)
    private StatementBlock statements;

    // Optional On Error statement (one per function)
    private OnErrorStatement onErrorStatement;

    // Begin of function scope marker statement
    private final MarkerStatement beginFunctionMarker;

    // Label to branch to for function Exit-statement
    private Method.Label exitLabel;

    // Indicates whether the function is a property getter or setter (properties need different
    // attributes generated)
    private boolean isProperty;

    // Indicates whether the function is compiler generated
    private boolean isGenerated;

    /*
     * Creates a new compilation information record (as the name suggests, for compiled functions
     * only).
     */
    private CompilationInformation() {
      params = new ArrayList<LocalVariableSymbol>();
      locals = new ArrayList<LocalVariableSymbol>();
      statements = new StatementBlock(definingObject.getScope());
      exitLabel = Method.newLabel();
      scope = (LocalScope) statements.getScope();

      // Will mark the start-of-scope for all parameters
      beginFunctionMarker = new MarkerStatement();
      statements.add(beginFunctionMarker);

      // Add a 'Me' variable for instance functions
      if (hasMeArgument()) {
        // Even though we call this variable 'Me', the Java debugger gets confused if it is not
        // called this. Specifically, JDI will report that there is a "this" and also a variable
        // named "Me.
        me = addVariable(getPosition(), "this", definingObject.getType(), false);
        me.setBeginScope(beginFunctionMarker);

        // Even though it will never be looked up via its scope, 'Me' needs to be entered because
        // the scope is responsible for setting variable scope information for debug information
        // generation.
        scope.enterSymbol(me);
      }
    }

    /*
     * Adds a function parameter.
     */
    private LocalVariableSymbol addParameter(long position, String parName, Type parType,
        boolean isRef) {
      LocalVariableSymbol param = addVariable(position, parName, parType, isRef);
      param.setBeginScope(beginFunctionMarker);
      referenceParameters.set(params.size(), isRef);
      params.add(param);

      if (scope.lookupShallow(parName) != null) {
        return null;
      }

      scope.enterSymbol(param);
      return param;
    }

    /*
     * Adds a new local variable.
     */
    private LocalVariableSymbol addLocalVariable(long position, String varName, Type varType,
        Scope varScope) {
      LocalVariableSymbol local = addVariable(position, varName, varType, false);
      locals.add(local);

      if (varScope.lookupShallow(varName) != null) {
        return null;
      }

      varScope.enterSymbol(local);
      return local;
    }

    /*
     * Adds a new variable (either parameter or local). This method should only be called from
     * within CompilationInformation.
     */
    private LocalVariableSymbol addVariable(long position, String varName, Type varType,
        boolean isRef) {
      short varIndex = varCount;
      varCount += (varType.isWideType() && !isRef) ? 2 : 1;
      return new LocalVariableSymbol(position, varName, varType, varIndex, isRef);
    }

    /*
     * Resolves the formal parameter list.
     */
    private void resolveParameters(Compiler compiler) {
      for (LocalVariableSymbol param : params) {
        param.resolve(compiler, FunctionSymbol.this);
        formalArgumentTypes.add(param.getType());
      }
    }

    /*
     * Resolves the function body (including local variables).
     */
    private void resolveBody(Compiler compiler) {
      if (statements != null) {
        // First resolve local variables
        for (LocalVariableSymbol local: locals) {
          local.resolve(compiler, FunctionSymbol.this);
        }

        // The resolve the actual function body
        statements.resolve(compiler, FunctionSymbol.this, null);

        if (onErrorStatement != null) {
          onErrorStatement.resolve(compiler, FunctionSymbol.this);
        }
      }
    }
  }

  // Object defining the function
  private ObjectSymbol definingObject;

  // List of formal argument types
  private final List<Type> formalArgumentTypes;

  // Indicates reference parameters.
  private final BitSet referenceParameters;

  // Result type of function (null for procedures)
  private Type resultType;

  // Additional information about the function body for compiled functions
  private CompilationInformation compilationInformation;

  // Function type
  private Type type;

  /**
   * Creates a new function symbol.
   *
   * @param position  source code start position of symbol
   * @param definingObject  defining object
   * @param name  function name
   */
  public FunctionSymbol(long position, ObjectSymbol definingObject, String name) {
    super(position, name);

    this.definingObject = definingObject;

    formalArgumentTypes = new ArrayList<Type>();
    referenceParameters = new BitSet();
  }

  /**
   * Marks the function as a compiled function.
   */
  public void setIsCompiled() {
    compilationInformation = new CompilationInformation();
  }

  /**
   * Marks the function as being abstract (interface function).
   */
  public void setIsAbstract() {
    compilationInformation.statements = null;
  }

  /**
   * Marks the function as a property getter or setter.
   */
  public void setIsProperty() {
    // This will cause generation of the @SimpleProperty annotation rather than @SimpleFunction.
    compilationInformation.isProperty = true;
  }

  /**
   * Marks the function as being compiler generated.
   */
  public void setIsGenerated() {
    Preconditions.checkNotNull(compilationInformation);
    compilationInformation.isGenerated = true;
  }

  /**
   * Sets the On Error statement for a compiled function.
   *
   * @param onErrorStatement  On-Error statement
   * @return  {@code false} if there is another On-Error statement in this
   *          function already, {@code true} otherwise
   */
  public boolean setOnErrorStatement(OnErrorStatement onErrorStatement) {
    Preconditions.checkNotNull(compilationInformation);

    if (compilationInformation.onErrorStatement != null) {
      return false;
    }

    compilationInformation.onErrorStatement = onErrorStatement;
    return true;
  }

  /**
   * Sets the function result type.
   *
   * @param type  function result type
   */
  public void setResultType(Type type) {
    resultType = type;
    if (compilationInformation != null) {
      // If this is a compiled function we also need to allocate a local variable of the same
      // name as the function to hold the function result
      LocalVariableSymbol returnVariable = addLocalVariable(getPosition(), getName(), type,
          getScope());
      MarkerStatement marker = new LocalVariableDefinitionStatement(returnVariable);
      compilationInformation.statements.add(marker);
      returnVariable.setBeginScope(marker);
      compilationInformation.returnVariable = returnVariable;
    }
  }

  /**
   * Change the defining object for this function. This is needed when moving an
   * event handler into a synthetic inner class.
   *
   * @param object  new defining object
   */
  public void changeDefiningObject(ObjectSymbol object) {
    definingObject = object;
  }

  /**
   * Indicates whether this function is a compiled function (as opposed to
   * loaded from a library).
   *
   * @return  {@code true} for compiled functions, {@code false} otherwise
   */
  public boolean isCompiled() {
    return compilationInformation != null;
  }

  /**
   * Indicates whether the functions is an instance or object function.
   *
   * @return  {@code true} for instance functions, {@code false} otherwise
   */
  public boolean hasMeArgument() {
    return false;
  }

  /**
   * Returns the function result type.
   *
   * @return  function result type or {@code null} for procedures
   */
  public Type getResultType() {
    return resultType;
  }

  /**
   * Adds a formal parameter.
   *
   * @param position  source code start position of symbol
   * @param parName  parameter name (can be {@code null} for loaded functions)
   * @param parType  parameter type
   * @param isRef  indicates reference parameter
   * @return  parameter symbol or {@code null} for redefiniton errors
   *          (note: will always be {code null} for loaded functions which does
   *          not indicate an error)
   */
  public LocalVariableSymbol addParameter(long position, String parName, Type parType,
      boolean isRef) {
    if (compilationInformation != null) {
      return compilationInformation.addParameter(position, parName, parType, isRef);
    } else {
      referenceParameters.set(formalArgumentTypes.size(), isRef);
      formalArgumentTypes.add(parType);
      return null;
    }
  }

  /**
   * Adds a local variable. May not be called for loaded functions!
   *
   * @param position  source code start position of symbol
   * @param varName  local variable name
   * @param varType  local variable type
   * @param scope  scope to enter local variable into
   * @return  local variable symbol
   */
  public LocalVariableSymbol addLocalVariable(long position, String varName, Type varType,
      Scope scope) {
    Preconditions.checkNotNull(compilationInformation);

    return compilationInformation.addLocalVariable(position, varName, varType, scope);
  }

  /**
   * Adds a temporary local variable. Should be used to allocated additional
   * variables needed for code generation. Must be called during the resolution
   * phase.
   *
   * @param varType  type of temporary
   * @return  temporary variable symbol
   */
  public LocalVariableSymbol addTempVariable(Type varType) {
    return compilationInformation.addVariable(Scanner.NO_POSITION, "<temp>", varType, false);
  }

  /**
   * Returns the object defining the data member.
   *
   * @return  defining object
   */
  public ObjectSymbol getDefiningObject() {
    return definingObject;
  }

  /**
   * Returns the function scope.
   *
   * @return  function scope
   */
  public Scope getScope() {
    return compilationInformation.scope;
  }

  /**
   * Returns the statements in the function body.
   *
   * @return  function body statement block (can be {@code null} for abstract
   *          functions)
   */
  public StatementBlock getFunctionStatements() {
    return compilationInformation.statements;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    if (type == null) {
      // For compiled functions resolve formal parameters (will resolve types needed to resolve
      // function type)
      if (compilationInformation != null) {
        compilationInformation.resolveParameters(compiler);
      }

      // Create and resolve function type
      type = new FunctionType(hasMeArgument(), formalArgumentTypes, referenceParameters,
          resultType);
      type.resolve(compiler);
    }
  }

  /**
   * Resolves the body of compiled functions.
   *
   * @param compiler  current compiler instance
   */
  public void resolveBody(Compiler compiler) {
    if (compilationInformation != null) {
      compilationInformation.resolveBody(compiler);
    }
  }

  /**
   * Returns the exit label for a function Exit-statement.
   *
   * @return  exit label
   */
  public Method.Label getExitLabel() {
    return compilationInformation.exitLabel;
  }

  /**
   * Returns the token to be expected after an Exit statement for the function.
   *
   * @return  exit token
   */
  public TokenKind getExitToken() {
    if (compilationInformation.isProperty) {
      return TokenKind.TOK_PROPERTY;
    } else if (resultType != null) {
      return TokenKind.TOK_FUNCTION;
    } else {
      return TokenKind.TOK_SUB;
    }
  }

  @Override
  public void generateRead(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  @Override
  public void generateWrite(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates code for the function.
   *
   * @param compiler  current compiler instance
   * @param cf  classfile to generate the code into
   */
  public void generate(Compiler compiler, ClassFile cf) {
    // By default all Simple functions are public
    short access = Method.ACC_PUBLIC;

    // Object functions are static
    if (!hasMeArgument()) {
      access |= Method.ACC_STATIC;
    }

    // Interface functions are abstract
    if (compilationInformation.statements == null) {
      access |= Method.ACC_ABSTRACT;
    }

    Method m = cf.newMethod(access, getName(), type.signature());
    // Mark function/property as a Simple function/property
    if (!compilationInformation.isGenerated) {
      m.getRuntimeVisibleAnnotationsAttribute().newAnnotation(
          'L' + RuntimeLoader.ANNOTATION_INTERNAL +
          (compilationInformation.isProperty ? "/SimpleProperty;" : "/SimpleFunction;"));
    }

    // If there is a function body then generate bytecode
    if (compilationInformation.statements != null) {
      try {
        m.startCodeGeneration();

        // If there is an On Error statement then we must explicitly initialize all local
        // variables to appease the verifier (even if they later re-initialized).
        if (compilationInformation.onErrorStatement != null) {
          for (LocalVariableSymbol local : compilationInformation.locals) {
            local.generateInitializer(m);
          }
        }

        // Need to copy reference parameters into their dereferenced temporary local variables
        for (LocalVariableSymbol param :compilationInformation.params) {
          // TODO: generate better code - only do this for used parameters
          param.readReferenceParameter(m);
        }

        Label startLabel = Method.newLabel();
        m.setLabel(startLabel);

        compilationInformation.statements.generate(m);

        m.setLabel(compilationInformation.exitLabel);

        // Need to write back local temps of reference parameters
        for (LocalVariableSymbol param :compilationInformation.params) {
          // TODO: generate better code - only do this for used parameters
          param.writeBackReferenceParameter(m);
        }

        if (compilationInformation.returnVariable == null) {
          m.generateInstrReturn();
        } else {
          compilationInformation.returnVariable.generateRead(m);
          compilationInformation.returnVariable.getType().generateReturn(m);
        }

        // Generate On Error statement (if there is one)
        if (compilationInformation.onErrorStatement != null) {

          Label handlerLabel = Method.newLabel();
          m.setLabel(handlerLabel);

          m.generateExceptionHandlerInfo(startLabel, compilationInformation.exitLabel, handlerLabel,
              "java/lang/Throwable");

          compilationInformation.onErrorStatement.generate(m);
        }

        // Generate local variable debug information
        if (compilationInformation.me != null) {
          generateLocalVarDebugInfo(m, compilationInformation.me);
        }
        for (LocalVariableSymbol param: compilationInformation.params) {
          generateLocalVarDebugInfo(m, param);
        }
        for (LocalVariableSymbol local: compilationInformation.locals) {
          generateLocalVarDebugInfo(m, local);
        }

        m.finishCodeGeneration();
      } catch (MethodTooBigException e) {
        compiler.error(Scanner.NO_POSITION, Error.errFunctionTooBig, getName(),
            definingObject.getType().internalName().replace('/', '.'));
      }
    }
  }

  /*
   * Generates debug information for a local variable.
   */
  private void generateLocalVarDebugInfo(Method m, LocalVariableSymbol local) {
    m.generateLocalVariableInformation(local.getStartScopeLabel(), local.getEndScopeLabel(),
        local.getName(), local.getType().signature(), local.getActualVarIndex());
  }

  // SymbolWithType implementation

  public final Type getType() {
    return type;
  }

  public final void setType(Type type) {
    this.type = type;
  }
}
