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
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.InstanceMember;
import com.google.devtools.simple.compiler.symbols.SymbolWithType;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.synthetic.ErrorSymbol;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.synthetic.ErrorType;

/**
 * This class represents an identifier expression without any further explicit
 * qualification (just a lone identifier).
 *
 * @author Herbert Czymontek
 */
public final class SimpleIdentifierExpression extends IdentifierExpression {

  /**
   * Creates a new simple, unqualified identifier expression
   * 
   * @param position  source code start position of expression
   * @param scope  scope at point of reference
   * @param identifier  identifier to resolve
   */
  public SimpleIdentifierExpression(long position, Scope scope, String identifier) {
    super(position, scope, null, identifier);
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Resolve identifier
    resolvedIdentifier = scope.lookupDeep(identifier);

    // Check result
    if (!checkFound(compiler)) {
      createErrorIdentifier();
    }

    resolvedIdentifier.resolve(compiler, currentFunction);

    // Check whether the resolved identifier is an instance member and therefore implies a Me
    // reference
    if (resolvedIdentifier instanceof InstanceMember) {
      ObjectSymbol memberClass = ((InstanceMember) resolvedIdentifier).getDefiningObject();

      if (currentFunction == null) {
        // This indicates that we looked up the 'wrong' symbol, e.g. one symbol hiding another from
        // an outer scope which would have been the 'correct' symbol
        compiler.error(getPosition(), Error.errInstanceMemberWithoutMe, identifier);
        createErrorIdentifier();
      } else {
        ObjectSymbol currentClass = currentFunction.getDefiningObject();
  
        MeExpression me = new MeExpression(getPosition(), currentClass);
        qualifyingExpression = me;
  
        // If the member is defined in an outer class then we need to use the
        // outer classes Me (we just check whether it is defined in the current
        // class hierarchy, and if it is not assume that it is in the outer class)
        ObjectType currentClassType = (ObjectType) currentClass.getType();
        ObjectType memberClassType = (ObjectType) memberClass.getType();
        if (!currentClassType.equals(memberClassType) &&
            !currentClassType.isBaseObject(memberClassType)) {
          me.setOuterClass(currentClass.getOuterclass());
        }
      }
    }

    // If the identifier has a type then the expression will have the same type
    if (resolvedIdentifier instanceof SymbolWithType) {
      type = ((SymbolWithType) resolvedIdentifier).getType();
      assert type != null;
      type.resolve(compiler);
    }

    return fold(compiler, currentFunction);
  }

  private void createErrorIdentifier() {
    resolvedIdentifier = new ErrorSymbol(identifier);
    scope.enterSymbol(resolvedIdentifier);
    type = ErrorType.errorType;
  }
  
  @Override
  public String toString() {
    return qualifyingExpression != null ? qualifyingExpression.toString() + '.' + identifier :
        identifier;  // COV_NF_LINE
  }
}
