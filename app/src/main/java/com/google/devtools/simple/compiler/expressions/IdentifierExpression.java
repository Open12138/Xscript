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
import com.google.devtools.simple.compiler.scopes.synthetic.ErrorScope;
import com.google.devtools.simple.compiler.symbols.DataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.InstanceFunctionSymbol;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.compiler.symbols.NamespaceSymbol;
import com.google.devtools.simple.compiler.symbols.PropertySymbol;
import com.google.devtools.simple.compiler.types.VariantType;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.ConstantDataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.Symbol;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.util.Preconditions;

/**
 * This class is the superclass for identifier expressions.
 * <p/>
 * <p>An identifier expression is an expression that consists of an identifier
 * with or without any further qualification.
 *
 * @author Herbert Czymontek
 */
public abstract class IdentifierExpression extends Expression implements ExpressionWithScope {

    // Qualifying expression for the identifier (can be null for simple identifiers)
    protected Expression qualifyingExpression;

    // Identifier to resolve
    protected final String identifier;

    // Scope at point of identifier reference (can be null for qualified identifiers)
    protected Scope scope;

    // Symbol of resolved identifier
    protected Symbol resolvedIdentifier;

    /**
     * Creates a new identifier expression.创建一个新的标识符表达式。
     *
     * @param position             source code start position of expression 源代码起始位置的表达
     * @param scope                scope at point osf identifier reference  标识符引用的作用域
     * @param qualifyingExpression qualifying expression for identifier  符合条件的表达式标识符
     * @param identifier           identifier to resolve    解析的标识符s
     */
    public IdentifierExpression(long position, Scope scope, Expression qualifyingExpression,
                                String identifier) {
        super(position);

        this.scope = scope;
        this.qualifyingExpression = qualifyingExpression;
        this.identifier = identifier;
    }

    /**
     * Returns the resolved identifier.
     *
     * @return resolved identifier
     */
    public Symbol getResolvedIdentifier() {
        return resolvedIdentifier;
    }

    public Scope getScope() {
        if (resolvedIdentifier instanceof NamespaceSymbol) {
            // If the identifier is a namespace identifier then return its scope
            return ((NamespaceSymbol) resolvedIdentifier).getScope();
        } else if (type != null && type instanceof ObjectType) {
            // If the identifier has an object type then return the object's scope
            return ((ObjectType) type).getScope();
        } else {
            // Else return the scope at the point of reference
            return scope;
        }
    }

    /**
     * Checks that the identifier expression was successfully resolved.
     * Reports an error otherwise.
     *
     * @param compiler current compiler instance
     * @return {@code false} if an error was reported, {@code true} otherwise
     */
    protected boolean checkFound(Compiler compiler) {
        if (resolvedIdentifier == null) {
            if (!(scope instanceof ErrorScope)) {
                compiler.error(getPosition(), Error.errIdentifierNotFound, identifier);
            }
            return false;
        }

        return !resolvedIdentifier.isErrorSymbol();
    }

    @Override
    public boolean isAssignable() {
        if (resolvedIdentifier instanceof LocalVariableSymbol) {
            return true;
        }

        if (resolvedIdentifier instanceof DataMemberSymbol) {
            return !(resolvedIdentifier instanceof ConstantDataMemberSymbol);
        }

        if (resolvedIdentifier instanceof PropertySymbol) {
            return true;
        }

        return resolvedIdentifier.isErrorSymbol();
    }

    @Override
    public Expression fold(Compiler compiler, FunctionSymbol currentFunction) {
        if (resolvedIdentifier instanceof ConstantDataMemberSymbol) {
            return ((ConstantDataMemberSymbol) resolvedIdentifier).getConstant();
        } else {
            return this;
        }
    }

    @Override
    public void generate(Method m) {
        if (qualifyingExpression != null) {
            qualifyingExpression.generate(m);
        }

        if (resolvedIdentifier == null) {
            // runtime resolution
            Preconditions.checkState(type.isVariantType());
            m.generateInstrLdc(identifier);
            m.generateInstrInvokevirtual(VariantType.VARIANT_INTERNAL_NAME, "dataMember",
                    "(Ljava/lang/String;)L" + VariantType.VARIANT_INTERNAL_NAME + ';');

        } else {
            resolvedIdentifier.generateRead(m);
        }
    }

    @Override
    public void generatePrepareWrite(Method m) {
        if (qualifyingExpression != null) {
            qualifyingExpression.generate(m);
        }

        if (resolvedIdentifier == null) {
            m.generateInstrLdc(identifier);
        }
    }

    @Override
    public void generateWrite(Method m) {
        if (resolvedIdentifier == null) {
            // runtime resolution
            Preconditions.checkState(type.isVariantType());
            m.generateInstrInvokevirtual(VariantType.VARIANT_INTERNAL_NAME, "dataMember",
                    "(Ljava/lang/String;L" + VariantType.VARIANT_INTERNAL_NAME + ";)V");
        } else {
            resolvedIdentifier.generateWrite(m);
        }
    }

    @Override
    public void generatePrepareInvoke(Method m) {
        if (qualifyingExpression != null) {
            qualifyingExpression.generate(m);
        }
    }

    @Override
    public void generateInvoke(Method m) {
        FunctionSymbol functionSymbol = (FunctionSymbol) resolvedIdentifier;
        ObjectSymbol functionObjectSymbol = functionSymbol.getDefiningObject();
        String functionClassInternalName = functionObjectSymbol.getType().internalName();
        String functionName = functionSymbol.getName();
        String functionSignature = functionSymbol.getType().signature();

        // Determine appropriate invocation byte code
        if (functionSymbol instanceof InstanceFunctionSymbol) {
            if (functionObjectSymbol.isInterface()) {
                m.generateInstrInvokeinterface(functionClassInternalName, functionName, functionSignature);
            } else {
                m.generateInstrInvokevirtual(functionClassInternalName, functionName, functionSignature);
            }
        } else {
            m.generateInstrInvokestatic(functionClassInternalName, functionName, functionSignature);
        }
    }
}
