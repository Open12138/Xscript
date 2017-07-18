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

import com.google.devtools.simple.runtime.components.Component;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.Layout;
import com.google.devtools.simple.runtime.components.Panel;
import com.google.devtools.simple.runtime.variants.ObjectVariant;
import com.google.devtools.simple.runtime.variants.Variant;

import android.view.View;
import android.view.ViewGroup;

/**
 * A container that can be placed within a Form or another container.
 *
 * @author Herbert Czymontek
 */
public class PanelImpl extends ViewComponent implements Panel, ViewComponentContainer {

  // Layout
  protected LayoutImpl viewLayout;

  // When this is true, attempting to change the Layout property results in an error
  private boolean layoutFixed;

  // Backing for Layout property
  private Variant layout;

  // Backing for background color
  private int backgroundColor;

  /**
   * Creates a new Panel component.
   *
   * @param container  container, component will be placed in
   */
  public PanelImpl(ComponentContainer container) {
    super(container);

    layoutFixed = false;
  }

  @Override
  protected View createView() {
    return null;
  }

  // Panel implementation

  @Override
  public int BackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    // Due to the ordering of Simple properties, the viewLayout may not have been set yet.
    if (viewLayout != null) {
      viewLayout.getLayoutManager().setBackgroundColor(argb);
    }
  }

  @Override
  public Variant Layout() {
    return layout;
  }

  @Override
  public void Layout(Variant layoutType) {
    if (layoutFixed) {
      throw new IllegalStateException(
          "The Layout property may not be changed after " +
          "children components have been added to its container");
    }

    switch (layoutType.getInteger()) {
      default:
        throw new IllegalArgumentException("Unknown layout");

      case Component.LAYOUT_LINEAR:
        viewLayout = new LinearLayoutImpl(this);
        break;

      case Component.LAYOUT_TABLE:
        viewLayout = new TableLayoutImpl(this);
        break;

      case Component.LAYOUT_FRAME:
        viewLayout = new FrameLayoutImpl(this);
        break;
    }
    layout = ObjectVariant.getObjectVariant(viewLayout);

    BackgroundColor(backgroundColor);

    addToContainer();
  }

  protected void addToContainer() {
    // Could not add this component earlier in ViewComponent constructor - do it now
    getComponentContainer().addComponent(this);
  }

  // ComponentContainer implementation

  @Override
  public ViewGroup getLayoutManager() {
    return viewLayout.getLayoutManager();
  }

  @Override
  public void addComponent(Component component) {
    // Prevent the layout from being changed now that components are being added to it
    layoutFixed = true;

    viewLayout.addComponent((ViewComponent) component);
  }

  @Override
  public Layout getLayout() {
    return viewLayout;
  }

  // ViewComponent implementation

  @Override
  public View getView() {
    return viewLayout == null ? null : viewLayout.getLayoutManager();
  }
}
