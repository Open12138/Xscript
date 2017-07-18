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
import com.google.devtools.simple.runtime.components.FrameLayout;

/**
 * Frame layout for Simple components.
 * 
 * @author David Foster
 */
public class FrameLayoutImpl extends LayoutImpl implements FrameLayout {
  
  /**
   * Creates a new frame layout.
   * 
   * @param container  view container
   */
  FrameLayoutImpl(ViewComponentContainer container) {
    super(new android.widget.FrameLayout(ApplicationImpl.getContext()), container);
  }

  @Override
  public void addComponent(ViewComponent component) {
    getLayoutManager().addView(component.getView(), new android.widget.FrameLayout.LayoutParams(
        android.view.ViewGroup.LayoutParams.FILL_PARENT,
        android.view.ViewGroup.LayoutParams.FILL_PARENT));
  }
}
