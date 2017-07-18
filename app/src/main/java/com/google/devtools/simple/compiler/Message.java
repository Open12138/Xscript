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

package com.google.devtools.simple.compiler;

import com.google.devtools.simple.compiler.scanner.Scanner;

import java.io.PrintStream;

/**
 * This is the superclass for messages (like errors and warnings) reported by
 * the compiler.
 * <p/>
 * <p>Each message is created from a template. The template can have parameters
 * which consist of a percent sign followed by a parameter number. Parameter
 * numbers start with 1. During message construction the message template
 * parameters are substituted with the actual message parameters.
 *
 * @author Herbert Czymontek
 */
public abstract class Message {
    // Source position as encoded by Scanner.
    private final long position;

    // Actual message
    private final String message;

    /**
     * Creates a new message.
     *
     * @param position source position
     * @param message  message template
     * @param params   parameters for message
     */
    protected Message(long position, String message, String... params) {
        this.position = position;
        this.message = applyParameters(message, params);
    }

    /**
     * Returns the message kind, e.g. "error" for error messages.
     *
     * @return message kind
     */
    protected abstract String messageKind();

    /**
     * Prints the message.
     *
     * @param compiler current compiler instance
     * @param out      output stream to print to
     */
    public final void print(Compiler compiler, PrintStream out) {
        String filename = compiler.fileIndexToPath(Scanner.getFileIndex(position));
        String decodedPosition = filename + ':';
        if (position != Scanner.NO_POSITION) {
            decodedPosition += Scanner.getLine(position) + ":";
        }
        out.println(decodedPosition + ' ' + messageKind() + ": " + message);
        Main.state(decodedPosition + ' ' + messageKind() + ": " + message);
    }

    /**
     * Localizes a named message or, if no localization is available, initializes
     * it with a default message.
     *
     * @param msgName        name of message
     * @param defaultMessage default message
     * @return localized message
     */
    public final static String localize(String msgName, String defaultMessage) {
        // TODO: read localized messages from a properties file
        return defaultMessage;
    }

    /*
     * Applies any parameters to the variables in the message template.
     */
    private static String applyParameters(String message, String... params) {
        int paramCnt = params.length;
        for (int i = 0; i < paramCnt; i++) {
            message = applyParameter(message, i + 1, params[i]);
        }
        return message;
    }

    /*
     * Applies a parameter to a variable in the message.
     */
    private static String applyParameter(String message, int param, String str) {
        return message.replaceAll("%" + param, str);
    }
}
