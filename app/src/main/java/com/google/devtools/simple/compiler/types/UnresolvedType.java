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
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.Main;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.expressions.IdentifierExpression;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.Symbol;
import com.google.devtools.simple.util.Preconditions;

/**
 * Represents unresolved object types.
 *
 * @author Herbert Czymontek
 */
public final class UnresolvedType extends ObjectType {

    // Type name expression
    private Expression expression;

    /**
     * Creates a new unresolved object type.
     *
     * @param expression unresolved type name expression
     */
    public UnresolvedType(Expression expression) {
        super(null);

        this.expression = expression;
    }

    @Override
    public void resolve(Compiler compiler) {
        expression = expression.resolve(compiler, null);
        if (!(expression instanceof IdentifierExpression)) {
            compiler.error(expression.getPosition(), Error.errObjectTypeNeeded, expression.toString());
        } else {
            Symbol symbol = ((IdentifierExpression) expression).getResolvedIdentifier();
            Preconditions.checkNotNull(symbol);

            if (!(symbol instanceof ObjectSymbol)) {
                // If the symbol is the special error symbol then there is no need to report another error
                if (!symbol.isErrorSymbol()) {
                    compiler.error(expression.getPosition(), Error.errObjectTypeNeeded, symbol.getName());
                    Main.state("2222222222222222");//Timer出错
                }
            } else {
                // This is to make sure that loaded classes are resolved lazily. We don't want to do this
                // for compiled classes to avoid ugly recursive resolution detection.
                objectSymbol = (ObjectSymbol) symbol;
                if (objectSymbol.isLoaded()) {
                    objectSymbol.resolve(compiler, null);
                }
            }
        }
    }
}
