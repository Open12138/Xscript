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

package com.google.devtools.simple.runtime.components;

import com.google.devtools.simple.runtime.annotations.SimpleComponent;
import com.google.devtools.simple.runtime.annotations.SimpleEvent;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;

/**
 * Simple Button component.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
public interface Button extends TextComponent {

  // Component declaration

  /**
   * Default Click event handler.
   */
  @SimpleEvent
  void Click();

  /**
   * Default GotFocus event handler.
   */
  @SimpleEvent
  void GotFocus();

  /**
   * Default LostFocus event handler.
   */
  @SimpleEvent
  void LostFocus();

  /**
   * Image property getter method.
   *
   * @return  image path
   */
  @SimpleProperty
  String Image();

  /**
   * Image property setter method.
   *
   * @param imagePath   a pathname identifying an image (either a content: URI or
   *                    a resource path, for now)
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_ASSET,
                  initializer = "\"\"")
  void Image(String imagePath);

  /**
   * Enabled property getter method.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty
  boolean Enabled();

  /**
   * Enabled property setter method.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
                  initializer = "True")
  void Enabled(boolean enabled);
}
