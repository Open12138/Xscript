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

package com.google.devtools.simple.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark Simple properties.
 * 
 * <p>Both, the getter and the setter method of the property need to be marked
 * with this annotation.
 * 
 * @author Herbert Czymontek
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimpleProperty {

  /**
   * Property editor types.
   */
  static final String PROPERTY_TYPE_ASSET = "simple.asset";
  static final String PROPERTY_TYPE_BOOLEAN = "simple.boolean";
  static final String PROPERTY_TYPE_COLOR = "simple.color";
  static final String PROPERTY_TYPE_DOUBLE = "simple.double";
  static final String PROPERTY_TYPE_GRAVITY = "simple.gravity";
  static final String PROPERTY_TYPE_HORIZONTAL_ALIGNMENT = "simple.halign";
  static final String PROPERTY_TYPE_INTEGER = "simple.integer";
  static final String PROPERTY_TYPE_LAYOUT = "simple.layout";
  static final String PROPERTY_TYPE_LONG = "simple.long";
  static final String PROPERTY_TYPE_SINGLE = "simple.single";
  static final String PROPERTY_TYPE_STRING = "simple.string";
  static final String PROPERTY_TYPE_TEXT = "simple.text";
  static final String PROPERTY_TYPE_TEXTJUSTIFICATION = "simple.justification";
  static final String PROPERTY_TYPE_TYPEFACE = "simple.typeface";
  static final String PROPERTY_TYPE_VERTICAL_ALIGNMENT = "simple.valign";

  /**
   * Returns a more specialized type for the property than its runtime type
   * (for use by IDEs).
   *
   * @return  property type
   */
  String type() default PROPERTY_TYPE_TEXT;

  /**
   * Initial property value.
   * 
   * @return  initial property value
   */
  String initializer() default "";
}
