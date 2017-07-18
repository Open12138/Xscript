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
import com.google.devtools.simple.runtime.variants.Variant;

/**
 * Root component container. All other components need to be added directly
 * or indirectly to a form.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
public interface Form extends VisibleComponent, ComponentContainer {

  /**
   * Default Keyboard event handler.
   *
   * @param keycode  constant identifying pressed key (@see Component)
   */
  @SimpleEvent
  void Keyboard(int keycode);

  /**
   * Default Menu event handler.
   *
   * @param caption  string identifying selected menu item
   */
  @SimpleEvent
  void MenuSelected(String caption);

  /**
   * Default touch gesture event handler.
   *
   * @param direction  constant identifying direction of touch gesture
   *                   (@see Component)
   */
  @SimpleEvent
  void TouchGesture(int direction);

  /**
   * BackgroundImage property setter method: sets a background image for the
   * form.
   *
   * @param imagePath  path and name of image
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_ASSET,
                  initializer = "\"\"")
  void BackgroundImage(String imagePath);

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
   * @param layoutType  one of {@link Component#LAYOUT_LINEAR}, or
   *                    {@link Component#LAYOUT_TABLE}, or
   *                    {@link Component#LAYOUT_FRAME}
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_LAYOUT,
                  initializer = Component.LAYOUT_LINEAR + "")
  void Layout(Variant layoutType);

  /**
   * Indicates whether the content of the form is scrollable.
   *
   * @return  {@code true} for scrollable, {@code false} otherwise
   */
  @SimpleProperty
  boolean Scrollable();

  /**
   * Makes the content of the form scrollable (or not).
   *
   * @param scrollable  {@code true} for scrollable, {@code false} otherwise
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
                  initializer = "False")
  void Scrollable(boolean scrollable);

  /**
   * Title property getter method.
   *
   * @return  form caption
   */
  @SimpleProperty
  String Title();

  /**
   * Title property setter method: sets a new caption for the form in the
   * form's title bar.
   *
   * @param title  new form caption
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_STRING,
                  initializer = "\"\"")
  void Title(String title);

  /**
   * Height property getter method (read-only property).
   *
   * @return  height property used by the layout
   */
  @SimpleProperty
  int Height();

  /**
   * Width property getter method (read-only property).
   *
   * @return  width property used by the layout
   */
  @SimpleProperty
  int Width();

  // Overriding property default values

  @Override
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_COLOR,
                  initializer = Component.COLOR_WHITE + "")
  void BackgroundColor(int color);
}
