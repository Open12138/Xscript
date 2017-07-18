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
import com.google.devtools.simple.compiler.symbols.SymbolWithType;
import com.google.devtools.simple.compiler.symbols.synthetic.ErrorSymbol;
import com.google.devtools.simple.compiler.types.VariantType;
import com.google.devtools.simple.compiler.scopes.ObjectScope;
import com.google.devtools.simple.compiler.types.synthetic.ErrorType;

/**
 * This class represents an explicitly qualified identifier expression.
 * 
 * @author Herbert Czymontek
 */
public final class QualifiedIdentifierExpression extends IdentifierExpression {

  /**
   * Creates a new qualified identifier expression.
   * 
   * @param position  source code start position of expression
   * @param qualifyingExpression  qualifying expression
   * @param identifier  identifier to resolve
   */
  public QualifiedIdentifierExpression(long position, Expression qualifyingExpression,
      String identifier) {
    super(position, null, qualifyingExpression, identifier);
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    qualifyingExpression = qualifyingExpression.resolve(compiler, currentFunction);

    // If the qualifying expression has a variant type then the name will be resolved at runtime
    if (qualifyingExpression.type != null && qualifyingExpression.type.isVariantType()) {
      // We set the type to variant deferring resolution to runtime
      type = VariantType.variantType;
      return this;
    } else if (qualifyingExpression instanceof ExpressionWithScope) {
      // Resolve the identifier within the scope of the qualifying expression
      scope = ((ExpressionWithScope) qualifyingExpression).getScope();
      if (scope instanceof ObjectScope) {
        // If the scope belongs to an object then we need to lookup within the object hierarchy
        resolvedIdentifier = ((ObjectScope) scope).lookupInObject(identifier);
      } else {
        // Otherwise a shallow lookup (just within the given scope) is enough
        resolvedIdentifier = scope.lookupShallow(identifier);
      }

      // Make sure the identifier was successfully resolved or report an error
      if (!checkFound(compiler)) {
        resolvedIdentifier = new ErrorSymbol(identifier);
        scope.enterSymbol(resolvedIdentifier);
      }

      resolvedIdentifier.resolve(compiler, currentFunction);

      // If the resolved identifier has a type then make it the type of this expression
      if (resolvedIdentifier instanceof SymbolWithType) {
        type = ((SymbolWithType) resolvedIdentifier).getType();
        type.resolve(compiler);
      }
    } else {
      compiler.error(getPosition(), Error.errIdentifierDoesNotResolveToSymbol, toString());
      type = ErrorType.errorType;
    }

    return fold(compiler, currentFunction);
  }

  @Override
  public boolean isAssignable() {
    // For runtime resolution the resolvedIdentifier is null. In good faith we assume that it
    // is assignable.
    return resolvedIdentifier == null ? true : super.isAssignable();
  }

  @Override
  public String toString() {
    return qualifyingExpression.toString() + '.' + identifier;
  }
}
