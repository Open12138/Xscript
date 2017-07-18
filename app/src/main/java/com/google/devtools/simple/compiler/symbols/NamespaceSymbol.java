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

import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.scopes.GlobalScope;
import com.google.devtools.simple.compiler.scopes.NamespaceScope;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.util.Files;
import com.google.devtools.simple.util.Preconditions;

import java.io.File;

/**
 * Symbol used for defining namespaces.
 * 
 * @author Herbert Czymontek
 */
public final class NamespaceSymbol extends Symbol implements SymbolCanBePartOfQualifiedIdentifier {

  // Enclosing namespace
  private NamespaceSymbol outerNamespace;

  // Namespace scope
  private Scope scope;

  /**
   * Factory method for namespace symbols.
   * 
   * @param compiler  current compiler instance
   * @param packageName  package name of namespace in internal format
   * @return  namespace symbol
   */
  public static NamespaceSymbol getNamespaceSymbol(Compiler compiler, String packageName) {
    // Search for an existing namespace symbol always starts with the global/root namespace
    NamespaceSymbol outerNamespace = compiler.getGlobalNamespaceSymbol();

    for (;;) {
      // Scope to look in for next package
      Scope scope = outerNamespace.getScope();
      int index = packageName.indexOf('.');
      if (index == -1) {
        // Last package component
        NamespaceSymbol namespace = (NamespaceSymbol)scope.lookupShallow(packageName);
        return namespace == null ? new NamespaceSymbol(packageName, outerNamespace) : namespace;
      } else {
        // Package prefix
        String name = packageName.substring(0, index);
        packageName = packageName.substring(index + 1);
        NamespaceSymbol namespace = (NamespaceSymbol)scope.lookupShallow(name);
        outerNamespace = namespace == null ? new NamespaceSymbol(name, outerNamespace) : namespace;
      }
    }
  }

  /*
   * Creates a new nested namespace symbol.
   */
  private NamespaceSymbol(String name, NamespaceSymbol outerNamespace) {
    super(Scanner.NO_POSITION, name);
    Preconditions.checkArgument(outerNamespace != null);

    this.outerNamespace = outerNamespace;

    Scope outerScope = outerNamespace.getScope();
    scope = new NamespaceScope(outerScope);
    outerScope.enterSymbol(this);
  }

  /**
   * Creates a new root namespace symbol for the global scope.
   * 
   * <p>Note that this constructor may only be called once for any compiler
   * instance, preferably in its constructor.
   */
  public NamespaceSymbol() {
    super(Scanner.NO_POSITION, null);
    scope = new GlobalScope();
  }

  public Scope getScope() {
    return scope;
  }

  @Override
  public void generateRead(Method m) {
  }

  @Override
  public void generateWrite(Method m) {
  }

  /**
   * Generates the directory structure for the namespace on the filesystem.
   * 
   * @param rootDir  root directory for namespace structure
   * @return  file descriptor for namespace
   */
  public File generate(File rootDir) {
    if (outerNamespace == null) {
      return rootDir;
    }

    return Files.createDirectory(outerNamespace.generate(rootDir), getName());
  }

  /**
   * Returns the internal name of the namespace.
   * 
   * @return  internal namespace name
   */
  public String internalName() {
    return (outerNamespace == null) ? "" : (outerNamespace.internalName() + getName() + '/');
  }

  /*
   * Returns the outer namespaces for toString().
   */
  private String prependOuterNamespaceName() {
    return (outerNamespace == null) ? "" :
        (outerNamespace.prependOuterNamespaceName() + getName() + ':');
  }
  
  @Override
  public String toString() {
    return "<namespace> " + prependOuterNamespaceName();
  }
}
