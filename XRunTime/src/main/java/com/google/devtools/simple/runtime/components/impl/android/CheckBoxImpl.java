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
import com.google.devtools.simple.runtime.components.CheckBox;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Android implementation of Simple CheckBox component.
 *
 * @author Herbert Czymontek
 */
public final class CheckBoxImpl extends TextViewComponent
    implements CheckBox, OnCheckedChangeListener, OnFocusChangeListener {

  /**
   * Creates a new CheckBox component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public CheckBoxImpl(ComponentContainer container) {
    super(container);
  }

  @Override
  protected View createView() {
    android.widget.CheckBox view = new android.widget.CheckBox(ApplicationImpl.getContext());

    // Listen to focus changes
    view.setOnFocusChangeListener(this);
    view.setOnCheckedChangeListener(this);

    return view;
  }

  // CheckBox implementation

  @Override
  public void Changed() {
    EventDispatcher.dispatchEvent(this, "Changed");
  }

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
    view.invalidate();
  }

  @Override
  public boolean Value() {
    return ((android.widget.CheckBox) getView()).isChecked();
  }

  @Override
  public void Value(boolean value) {
    android.widget.CheckBox view = (android.widget.CheckBox) getView();
    view.setChecked(value);
    view.invalidate();
  }

  // OnCheckedChangeListener implementation

  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Changed();
  }

  // OnFocusChangeListener implementation

  public void onFocusChange(View previouslyFocused, boolean gainFocus) {
    if (gainFocus) {
      GotFocus();
    } else {
      LostFocus();
    }
  }
}
