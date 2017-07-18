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
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.Main;
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.scopes.ObjectScope;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.scopes.synthetic.ErrorScope;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.Type;

/**
 * Symbol for event handlers.
 *
 * @author Herbert Czymontek
 */
public final class EventHandlerSymbol extends FunctionSymbol implements InstanceMember {

    // Data member or class name that event handler shall be applied to
    private final String eventTargetName;

    // Event name
    private final String eventName;

    /**
     * Creates a new event handler symbol.
     *
     * @param position        source code start position of symbol
     * @param objectSymbol    defining object
     * @param eventTargetName data member or class name that the event handler
     *                        shall be applied to
     * @param eventName       event handler name
     */
    public EventHandlerSymbol(long position, ObjectSymbol objectSymbol,
                              String eventTargetName, String eventName) {
        super(position, objectSymbol, eventTargetName + '$' + eventName);

        this.eventTargetName = eventTargetName;
        this.eventName = eventName;

        setIsCompiled();
    }

    /**
     * Returns the name of the target the event handler is being applied to.
     *
     * @return name of event handler target
     */
    public String getEventTargetName() {
        return eventTargetName;
    }

    /**
     * Returns the name of the event this handler implements.
     *
     * @return name of event
     */
    public String getEventName() {
        return eventName;
    }

    @Override
    public TokenKind getExitToken() {
        return TokenKind.TOK_EVENT;
    }

    @Override
    public boolean hasMeArgument() {
        return !(eventTargetName.equals(getDefiningObject().getName()) && eventName.equals("Load"));
    }

    @Override
    public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
        super.resolve(compiler, currentFunction);

        ObjectSymbol definingObject = getDefiningObject();
        if (eventTargetName.equals(definingObject.getName())) {
            // Class initialization event handlers
            if (eventName.equals("Load")) {
                definingObject.setObjectLoadEventHandler(this);
                return;
            } else if (eventName.equals("Initialize")) {
                definingObject.setInstanceInitializeEventHandler(this);
                return;
            }
        }

        // Data member bound event handler

        Symbol symbol = definingObject.getScope().lookupShallow(eventTargetName);
        if (symbol == null) {
            compiler.error(getPosition(), Error.errUndefinedSymbol, eventTargetName);
            return;
        }

        if (!(symbol instanceof DataMemberSymbol)) {
            compiler.error(getPosition(), Error.errDataMemberExpected, eventTargetName);
            return;
        }

        DataMemberSymbol targetDataMember = (DataMemberSymbol) symbol;
        Type targetDataMemberType = targetDataMember.getType();
        if (!targetDataMemberType.isObjectType()) {
            compiler.error(getPosition(), Error.errObjectTypeNeeded, targetDataMemberType.toString());
            return;
        }

        targetDataMemberType.resolve(compiler);

        Scope scope = ((ObjectType) targetDataMemberType).getScope();
        if (scope instanceof ErrorScope) {
            // Error already reported
            return;
        }

        Symbol event = ((ObjectScope) scope).lookupInObject(eventName);
        if (event == null || !(event instanceof EventSymbol)) {
            compiler.error(getPosition(), Error.errUndefinedSymbol, eventName);
        }
    }
}
