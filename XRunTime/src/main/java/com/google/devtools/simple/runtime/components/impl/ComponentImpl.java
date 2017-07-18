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

package com.google.devtools.simple.runtime.components.impl;

import com.google.devtools.simple.runtime.components.Component;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.events.EventDispatcher;

/**
 * Superclass of all Simple components.
 *
 * @author Herbert Czymontek
 */
public abstract class ComponentImpl implements Component {

  // Component container
  private final ComponentContainer componentContainer;

  /**
   * Creates a new component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component must be
   *                   the form)
   */
  protected ComponentImpl(ComponentContainer container) {
    componentContainer = container;
  }

  /**
   * Returns the component container holding this component.
   *
   * @return  component container or {@code null} for root components like
   *          form (for non-visible components the form will be returned)
   */
  protected ComponentContainer getComponentContainer() {
    return componentContainer;
  }

  @Override
  public void Initialize() {
    EventDispatcher.dispatchEvent(this, "Initialize");
  }
}
