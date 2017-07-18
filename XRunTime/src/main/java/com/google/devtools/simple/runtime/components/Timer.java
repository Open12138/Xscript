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

package com.google.devtools.simple.runtime.components;

import com.google.devtools.simple.runtime.annotations.SimpleComponent;
import com.google.devtools.simple.runtime.annotations.SimpleEvent;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;

/**
 * Simple Timer component.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
public interface Timer extends Component {

  /**
   * Default Timer event handler.
   */
  @SimpleEvent
  void Timer();

  /**
   * Interval property getter method.
   *
   * @return  timer interval in ms
   */
  @SimpleProperty
  int Interval();

  /**
   * Interval property setter method: sets the interval between timer events.
   *
   * @param interval  timer interval in ms
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_INTEGER,
                  initializer = "1000")
  void Interval(int interval);

  /**
   * Enabled property getter method.
   *
   * @return  {@code true} indicates a running timer, {@code false} a stopped
   *          timer
   */
  @SimpleProperty
  boolean Enabled();

  /**
   * Enabled property setter method: starts or stops the timer.
   *
   * @param enabled  {@code true} starts the timer, {@code false} stops it
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
                  initializer = "True")
  void Enabled(boolean enabled);
}
