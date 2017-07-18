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

package com.google.devtools.simple.compiler.util;

/**
 * Helper methods to deal with type names and signatures.
 *
 * @author Herbert Czymontek
 */
public final class Signatures {

    private Signatures() {
    }

    /*
     * Extracts the package name from a compound name (assuming the package name to be all but the
     * last component of the compound name).
     */
    private static String getPackageName(String name, char separator) {
        int index = name.lastIndexOf(separator);
        return index < 0 ? "" : name.substring(0, index);
    }

    /*
     * Extracts the class name from a compound name (assuming the class name to be the last component
     * of the compound name).
     */
    private static String getClassName(String name, char separator) {
        int index = name.lastIndexOf(separator);
        return index < 0 ? name : name.substring(index + 1);
    }

    /**
     * Returns the package name part of an dot-qualified class name.
     *
     * @param qualifiedName qualified class name
     * @return package name
     */
    public static String getPackageName(String qualifiedName) {
        return getPackageName(qualifiedName, '.');
    }

    /**
     * Returns the class name part of an dot-qualified class name.
     *
     * @param qualifiedName qualified class name
     * @return class name
     */
    public static String getClassName(String qualifiedName) {
        return getClassName(qualifiedName, '.');
    }

    /**
     * Returns the package name part of an internal name (according to the Java
     * VM specification).
     *
     * @param internalName Java VM style internal name
     * @return package name
     */
    public static String getInternalPackageName(String internalName) {
        return getPackageName(internalName, '/');
    }

    /**
     * Returns the class name part of an internal name (according to the Java
     * VM specification).
     *
     * @param internalName Java VM style internal name
     * @return class name
     */
    public static String getInternalClassName(String internalName) {
        return getClassName(internalName, '/');
    }
}
