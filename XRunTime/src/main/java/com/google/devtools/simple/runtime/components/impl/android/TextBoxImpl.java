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
import com.google.devtools.simple.runtime.components.TextBox;
import com.google.devtools.simple.runtime.events.EventDispatcher;
import com.google.devtools.simple.runtime.parameters.BooleanReferenceParameter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Android implementation of Simple text box component.
 *
 * @author Herbert Czymontek
 */
public final class TextBoxImpl extends TextViewComponent implements TextBox {

  // Backing for hint text
  private String hint;

  /**
   * Creates a new TextBox component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public TextBoxImpl(ComponentContainer container) {
    super(container);
  }

  @Override
  protected View createView() {
    EditText view = new EditText(ApplicationImpl.getContext());

    // Add a handler for input validation
    view.addTextChangedListener(new TextWatcher() {
      private boolean recursionGuard;
      private CharSequence beforeText;

      @Override
      public void afterTextChanged(Editable s) {
        if (!recursionGuard) {
          recursionGuard = true;
          BooleanReferenceParameter accept = new BooleanReferenceParameter(true);
          Validate(s.toString(), accept);
          if (!accept.get()) {
            s.replace(0, s.length(), beforeText);
          }
          recursionGuard = false;
        }
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeText = ((TextView) getView()).getText();
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
    });
    
    // Listen to focus changes
    view.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View previouslyFocused, boolean gainFocus) {
        if (gainFocus) {
          GotFocus();
        } else {
          LostFocus();
        }
      }      
    });

    return view;
  }

  // TextBox implementation

  @Override
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  @Override
  public void LostFocus() {
    EventDispatcher.dispatchEvent(this, "LostFocus");
  }

  @Override
  public void Validate(String sourceText, BooleanReferenceParameter accept) {
    EventDispatcher.dispatchEvent(this, "Validate", sourceText, accept);
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
}
