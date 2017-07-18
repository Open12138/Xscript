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

/**
 * Expression subclasses that need to know whether the value that they
 * represent is not used, need to implement this interface.
 *
 * @author Herbert Czymontek
 */
public interface ExpressionValueUnused {
  /**
   * Will be invoked when analysis determines this expression's value is unused
   * (will be discarded).
   *
   * @param compiler  current compiler instance
   */
  void valueUnused(Compiler compiler);
}
