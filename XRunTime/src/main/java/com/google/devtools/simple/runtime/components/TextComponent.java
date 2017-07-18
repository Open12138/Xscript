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

import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;

/**
 * Text based Simple component (like button, label, textbox etc.).
 *
 * @author Herbert Czymontek
 */
@SimpleObject
public interface TextComponent extends VisibleComponent {

  /**
   * FontBold property getter method.
   *
   * @return  {@code true} indicates bold, {@code false} normal
   */
  @SimpleProperty
  boolean FontBold();

  /**
   * FontBold property setter method.
   *
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
                  initializer = "False")
  void FontBold(boolean bold);

  /**
   * FontItalic property getter method.
   *
   * @return  {@code true} indicates italic, {@code false} normal
   */
  @SimpleProperty
  boolean FontItalic();

  /**
   * FontItalic property setter method.
   *
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
                  initializer = "False")
  void FontItalic(boolean italic);

  /**
   * FontSize property getter method.
   *
   * @return  font size in pixel
   */
  @SimpleProperty
  float FontSize();

  /**
   * FontSize property setter method.
   *
   * @param size  font size in pixel
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_SINGLE,
                  initializer = Component.FONT_DEFAULT_SIZE + "")
  void FontSize(float size);

  /**
   * FontTypeface property getter method.
   *
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty
  int FontTypeface();

  /**
   * FontTypeface property setter method.
   *
   * @param typeface  one of {@link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_TYPEFACE,
                  initializer = Component.TYPEFACE_DEFAULT + "")
  void FontTypeface(int typeface);

  /**
   * Justification property getter method.
   *
   * @return  one of {@link Component#JUSTIFY_LEFT},
   *          {@link Component#JUSTIFY_CENTER} or
   *          {@link Component#JUSTIFY_RIGHT}
   */
  @SimpleProperty
  int Justification();

  /**
   * Justification property setter method.
   *
   * @param justification  one of {@link Component#JUSTIFY_LEFT},
   *                       {@link Component#JUSTIFY_CENTER} or
   *                       {@link Component#JUSTIFY_RIGHT}
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_TEXTJUSTIFICATION,
                  initializer = Component.JUSTIFY_LEFT + "")
  void Justification(int justification);

  /**
   * Text property getter.
   *
   * @return  text box contents
   */
  @SimpleProperty
  String Text();

  /**
   * Text property setter: sets the text box's contents
   *
   * @param newtext  new text in text box
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_STRING,
                  initializer = "\"\"")
  void Text(String newtext);

  /**
   * TextColor property getter method.
   *
   * @return  text RGB color with alpha
   */
  @SimpleProperty
  int TextColor();

  /**
   * TextColor property setter method.
   *
   * @param argb  text RGB color with alpha
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_COLOR,
                  initializer = Component.COLOR_BLACK + "")
  void TextColor(int argb);
}
