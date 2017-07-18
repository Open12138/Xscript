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
import com.google.devtools.simple.runtime.components.EmailPicker;
import com.google.devtools.simple.runtime.components.impl.android.util.EmailAddressAdapter;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.app.Activity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AutoCompleteTextView;

/**
 * Text box using auto-completion to pick out an email address from contacts.
 * 
 * @author Sharon Perl
 */
public final class EmailPickerImpl extends TextViewComponent
    implements EmailPicker, OnFocusChangeListener {

  /**
   * Create a new EmailPicker component.
   * 
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public EmailPickerImpl(ComponentContainer container) {
    super(container);
  }

  @Override
  protected View createView() {
    Activity context = ApplicationImpl.getContext();
    AutoCompleteTextView view = new AutoCompleteTextView(context);
    view.setAdapter(new EmailAddressAdapter(context));

    // Listen to focus changes
    view.setOnFocusChangeListener(this);

    return view;
  }

  // EmailPicker implementation

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
