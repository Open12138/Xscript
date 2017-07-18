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

import com.google.devtools.simple.runtime.components.Layout;

import android.view.ViewGroup;

/**
 * The Layout interface provides methods for working with Simple
 * component layouts.
 *
 * @author Herbert Czymontek
 */
public abstract class LayoutImpl implements Layout {

  private final ViewComponentContainer container;
  private final ViewGroup layoutManager;
  
  /**
   * Creates a new layout.
   *
   * @param layoutManager  Android layout manager
   * @param container  view container
   */
  LayoutImpl(ViewGroup layoutManager, ViewComponentContainer container) {
    this.layoutManager = layoutManager;
    this.container = container;
  }

  /**
   * Returns the view group (which is a container with a layout manager)
   * associated with the layout.
   * 
   * @return  view group
   */
  protected final ViewGroup getLayoutManager() {
    return layoutManager;
  }

  /**
   * Returns container this layout is associated with.
   * 
   * @return  component container
   */
  protected final ViewComponentContainer getContainer() {
    return container;
  }

  /**
   * Adds the specified component to this layout.
   *
   * @param component  component to add
   */
  public abstract void addComponent(ViewComponent component);
}
