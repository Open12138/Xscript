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

package com.google.devtools.simple.runtime.components.impl.android.util;

import com.google.devtools.simple.runtime.components.Component;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Helper methods for manipulating {@link TextView} objects.
 * 
 * @author Herbert Czymontek
 */
public class TextViewUtil {

  private TextViewUtil() {
  }

  /**
   * TextView justification setter.
   * 
   * @param textview   text view instance
   * @param justification  one of {@link Component#JUSTIFY_LEFT},
   *                       {@link Component#JUSTIFY_CENTER} or
   *                       {@link Component#JUSTIFY_RIGHT}
   */
  public static void setJustification(TextView textview, int justification) {
    int gravity;
    switch (justification) {
      default:
        throw new IllegalArgumentException();

      case Component.JUSTIFY_LEFT:
        gravity = Gravity.LEFT;
        break;

      case Component.JUSTIFY_CENTER:
        gravity = Gravity.CENTER_HORIZONTAL;
        break;

      case Component.JUSTIFY_RIGHT:
        gravity = Gravity.RIGHT;
        break;
    }
    textview.setGravity(gravity);
    textview.invalidate();
  }

  /**
   * {@link TextView} background color setter.
   * 
   * @param textview   text view instance
   * @param argb  background RGB color with alpha
   */
  public static void setBackgroundColor(TextView textview, int argb) {
    textview.setBackgroundColor(argb);
    textview.invalidate();
  }

  /**
   * Returns the enabled state a {@link TextView}.
   * 
   * @param textview   text view instance
   * @return  {@code true} for enabled, {@code false} disabled
   */
  public static boolean isEnabled(TextView textview) {
    return textview.isEnabled();
  }

  /**
   * Enables a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  public static void setEnabled(TextView textview, boolean enabled) {
    textview.setEnabled(enabled);
    textview.invalidate();
  }

  /**
   * Returns the font weight for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @return  {@code true} indicates bold, {@code false} normal
   */
  public static boolean isFontBold(TextView textview) {
    return textview.getTypeface().isBold();
  }

  /**
   * Sets the font weight for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  public static void setFontBold(TextView textview, boolean bold) {
    setFontStyle(textview, bold, Typeface.BOLD);
  }

  /**
   * Returns the font style for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @return  {@code true} indicates italic, {@code false} normal
   */
  public static boolean isFontItalic(TextView textview) {
    return textview.getTypeface().isItalic();
  }

  /**
   * Sets the font style for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  public static void setFontItalic(TextView textview, boolean italic) {
    setFontStyle(textview, italic, Typeface.ITALIC);
  }

  /**
   * Returns the font size for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @return  font size in pixel
   */
  public static float getFontSize(TextView textview) {
    return textview.getTextSize();
  }

  /**
   * Sets the font size for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param size  font size in pixel
   */
  public static void setFontSize(TextView textview, float size) {
    textview.setTextSize(size);
    textview.invalidate();
  }

  /**
   * Sets the font typeface for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param typeface  one of @link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   */
  public static void setFontTypeface(TextView textview, int typeface) {
    Typeface tf;
    switch (typeface) {
      default:
        throw new IllegalArgumentException();

      case Component.TYPEFACE_DEFAULT:
        tf = Typeface.DEFAULT;
        break;

      case Component.TYPEFACE_SERIF:
        tf = Typeface.SERIF;
        break;

      case Component.TYPEFACE_SANSSERIF:
        tf = Typeface.SANS_SERIF;
        break;

      case Component.TYPEFACE_MONOSPACE:
        tf = Typeface.MONOSPACE;
        break;
    }
    textview.setTypeface(tf);
    textview.invalidate();
  }

  /**
   * Returns the text for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @return  text shown in text view
   */
  public static String getText(TextView textview) {
    return textview.getText().toString();
  }

  /**
   * Sets the text for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param text  new text to be shown
   */
  public static void setText(TextView textview, String text) {
    textview.setText(text);
    textview.invalidate();
  }

  /**
   * Sets the text color for a {@link TextView}.
   * 
   * @param textview   text view instance
   * @param argb  text RGB color with alpha
   */
  public static void setTextColor(TextView textview, int argb) {
    textview.setTextColor(argb);
    textview.invalidate();
  }

  /*
   * Sets or clears a style for the current font of the TextView.
   */
  private static void setFontStyle(TextView textview, boolean set, int styleMask) {
    Typeface tf = textview.getTypeface();
    if (tf == null) {
      tf = Typeface.DEFAULT;
    }

    int style = tf.getStyle() & ~styleMask;
    if (set) {
      style |= styleMask;
    }
    textview.setTypeface(Typeface.create(tf, style));
    textview.invalidate();
  }
}
