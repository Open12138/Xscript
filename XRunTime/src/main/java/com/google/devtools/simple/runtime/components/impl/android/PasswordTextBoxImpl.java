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

import com.google.devtools.simple.runtime.android.ApplicationImpl;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.PasswordTextBox;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

/**
 * Text box for entering passwords.
 *
 * @author Damon Kohler
 */
public final class PasswordTextBoxImpl extends TextViewComponent
    implements PasswordTextBox, OnFocusChangeListener {

  // Backing for hint text
  private String hint;

  /**
   * Creates a new PasswordTextBox component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public PasswordTextBoxImpl(ComponentContainer container) {
    super(container);
  }

  @Override
  protected View createView() {
    EditText view = new EditText(ApplicationImpl.getContext());

    // Listen to focus changes
    view.setOnFocusChangeListener(this);

    // Add a transformation method to hide password text.
    view.setTransformationMethod(new PasswordTransformationMethod());

    return view;
  }

  // PasswordTextBox implementation

  @Override
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  @Override
  public void LostFocus() {
    EventDispatcher.dispatchEvent(this, "LostFocus");
  }

  @Override
  public boolean Enabled() {
    return getView().isEnabled();
  }

  @Override
  public void Enabled(boolean enabled) {
    View view = getView();
    view.setEnabled(enabled);
    view.setFocusable(enabled);
    view.setFocusableInTouchMode(enabled);
    view.invalidate();
  }

  @Override
  public String Hint() {
    return hint;
  }

  @Override
  public void Hint(String newhint) {
    hint = newhint;
    EditText view = (EditText) getView();
    view.setHint(hint);
    view.invalidate();
  }

  // OnFocusChangeListener implementation

  @Override
  public void onFocusChange(View previouslyFocused, boolean gainFocus) {
    if (gainFocus) {
      GotFocus();
    } else {
      LostFocus();
    }
  }
}
