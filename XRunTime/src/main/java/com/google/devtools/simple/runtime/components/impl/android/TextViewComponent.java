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

package com.google.devtools.simple.runtime.components.impl.android;

import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.TextComponent;
import com.google.devtools.simple.runtime.components.impl.android.util.TextViewUtil;

import android.widget.TextView;

/**
 * Underlying base class for all components with Android text views.
 *
 * @author Herbert Czymontek
 * @author Sharon Perl
 */
public abstract class TextViewComponent extends ViewComponent implements TextComponent {

  // Backing for text justification
  private int justification;

  // Backing for background color
  private int backgroundColor;

  // Backing for font typeface
  private int fontTypeface;

  // Backing for text color
  private int textColor;

  /**
   * Creates a new TextBoxBase component
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  protected TextViewComponent(ComponentContainer container) {
    super(container);
  }

  @Override
  public int BackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    TextViewUtil.setBackgroundColor((TextView) getView(), argb);
  }

  @Override
  public boolean FontBold() {
    return TextViewUtil.isFontBold((TextView) getView());
  }

  @Override
  public void FontBold(boolean bold) {
    TextViewUtil.setFontBold((TextView) getView(), bold);
  }

  @Override
  public boolean FontItalic() {
    return TextViewUtil.isFontItalic((TextView) getView());
  }

  @Override
  public void FontItalic(boolean italic) {
    TextViewUtil.setFontItalic((TextView) getView(), italic);
  }

  @Override
  public float FontSize() {
    return TextViewUtil.getFontSize((TextView) getView());
  }

  @Override
  public void FontSize(float size) {
    TextViewUtil.setFontSize((TextView) getView(), size);
  }

  @Override
  public int FontTypeface() {
    return fontTypeface;
  }

  @Override
  public void FontTypeface(int typeface) {
    fontTypeface = typeface;
    TextViewUtil.setFontTypeface((TextView) getView(), typeface);
  }

  @Override
  public int Justification() {
    return justification;
  }

  @Override
  public void Justification(int newJustification) {
    justification = newJustification;
    TextViewUtil.setJustification((TextView) getView(), newJustification);
  }

  @Override
  public String Text() {
    return TextViewUtil.getText((TextView) getView());
  }

  @Override
  public void Text(String newtext) {
    TextViewUtil.setText((TextView) getView(), newtext);
  }

  @Override
  public int TextColor() {
    return textColor;
  }

  @Override
  public void TextColor(int argb) {
    textColor = argb;
    TextViewUtil.setTextColor((TextView) getView(), argb);
  }
}
