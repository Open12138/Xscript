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
import com.google.devtools.simple.runtime.components.Layout;
import com.google.devtools.simple.runtime.components.VisibleComponent;
import com.google.devtools.simple.runtime.components.impl.android.util.ViewUtil;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;
import com.google.devtools.simple.runtime.errors.IndexOutOfBoundsError;

import android.view.View;

/**
 * Underlying base class for all components with Android views.
 *
 * @author Herbert Czymontek
 */
public abstract class ViewComponent extends ComponentImpl implements VisibleComponent {

  // View
  private final View view;

  // Backing for properties
  private int column;
  private int row;

  /**
   * Creates new Android view component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component must be
   *                   the form)
   */
  protected ViewComponent(ComponentContainer container) {
    super(container);

    column = LAYOUT_NO_COLUMN;
    row = LAYOUT_NO_ROW;

    // Initializes and adds the component into its container
    view = createView();
    if (view != null) {
      getComponentContainer().addComponent(this);
    }
  }

  /**
   * Creates and initializes the underlying Android view.
   *
   * <p>Because this method is called indirectly by the component constructor,
   * the implementor should not assume anything about the state of the
   * instance and should not read any non-static fields.
   */
  protected abstract View createView();

  /**
   * Returns the {@link View} that is displayed in the UI.
   *
   * @return  view
   */
  public View getView() {
    return view;
  }

  @Override
  public int Column() {
    return column;
  }

  @Override
  public void Column(int newColumn) {
    if (column != LAYOUT_NO_COLUMN) {
      throw new IndexOutOfBoundsError();
    }

    column = newColumn;

    if (row != LAYOUT_NO_ROW) {
      Layout layout = getComponentContainer().getLayout();
      if (layout instanceof TableLayoutImpl) {
        ((TableLayoutImpl) layout).placeComponent(this);
      }
    }
  }

  @Override
  public int Height() {
    return ViewUtil.getHeight(getView());
  }

  @Override
  public void Height(int height) {
    ViewUtil.setHeight(getView(), height);
  }

  @Override
  public int Row() {
    return row;
  }

  @Override
  public void Row(int newRow) {
    if (row != LAYOUT_NO_ROW) {
      throw new IndexOutOfBoundsError();
    }

    row = newRow;
    if (column != LAYOUT_NO_COLUMN) {
      Layout layout = getComponentContainer().getLayout();
      if (layout instanceof TableLayoutImpl) {
        ((TableLayoutImpl) layout).placeComponent(this);
      }
    }
  }

  @Override
  public int Width() {
    return ViewUtil.getWidth(getView());
  }

  @Override
  public void Width(int width) {
    ViewUtil.setWidth(getView(), width);
  }
}
