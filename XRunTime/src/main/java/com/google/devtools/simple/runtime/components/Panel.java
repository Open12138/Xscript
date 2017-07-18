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
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;
import com.google.devtools.simple.runtime.variants.Variant;

/**
 * A container that can be placed within a Form or another container.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
public interface Panel extends VisibleComponent, ComponentContainer {

  /**
   * Layout property getter method.
   *
   * @return  layout instance
   */
  @SimpleProperty
  Variant Layout();

  /**
   * Layout property setter method: we assign a layout constant to the property
   * which converts the constant into a layout object instance.
   *
   * @param layoutType  one of {@link Component#LAYOUT_LINEAR},
   *                    {@link Component#LAYOUT_TABLE} or
   *                    {@link Component#LAYOUT_FRAME}
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_LAYOUT,
                  initializer = Component.LAYOUT_LINEAR + "")
  void Layout(Variant layoutType);

  // Overriding property default values

  @Override
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_COLOR,
                  initializer = Component.COLOR_WHITE + "")
  void BackgroundColor(int color);
}
