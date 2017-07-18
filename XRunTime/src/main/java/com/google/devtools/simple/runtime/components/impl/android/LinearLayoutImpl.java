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
import com.google.devtools.simple.runtime.components.Component;
import com.google.devtools.simple.runtime.components.LinearLayout;

/**
 * Linear layout for placing components horizontally or vertically.
 *
 * @author Herbert Czymontek
 */
public final class LinearLayoutImpl extends LayoutImpl implements LinearLayout {

  /**
   * Creates a new linear layout.
   *
   * @param container  view container
   */
  LinearLayoutImpl(ViewComponentContainer container) {
    super(new android.widget.RadioGroup(ApplicationImpl.getContext()), container);

    android.widget.LinearLayout layoutManager = (android.widget.LinearLayout) getLayoutManager();
    layoutManager.setWeightSum(1.0f);

    // Without turning off baseline alignment we cannot have containers containing only other
    // containers...
    layoutManager.setBaselineAligned(false);

    // Default property values
    Orientation(Component.LAYOUT_ORIENTATION_VERTICAL);
  }

  @Override
  public void Orientation(int newOrientation) {
    ((android.widget.LinearLayout) getLayoutManager()).setOrientation(
        newOrientation == Component.LAYOUT_ORIENTATION_HORIZONTAL ?
            android.widget.LinearLayout.HORIZONTAL :
            android.widget.LinearLayout.VERTICAL);
  }

  @Override
  public void addComponent(ViewComponent component) {
    float weight = 0.5f;
    getLayoutManager().addView(component.getView(), new android.widget.LinearLayout.LayoutParams(
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        weight));
  }
}
