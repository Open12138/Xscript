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
import com.google.devtools.simple.runtime.components.Label;

import android.view.View;
import android.widget.TextView;

/**
 * Label containing a text string or an image.
 *
 * @author Herbert Czymontek
 */
public final class LabelImpl extends TextViewComponent implements Label {

  /**
   * Creates a new Label component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public LabelImpl(ComponentContainer container) {
    super(container);
  }

  @Override
  protected View createView() {
    return new TextView(ApplicationImpl.getContext());
  }
}
