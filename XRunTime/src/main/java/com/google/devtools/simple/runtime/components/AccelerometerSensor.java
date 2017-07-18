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
 * Sensor to measure acceleration in 3 dimensions.
 *
 * @author Herbert Czymontek
 */
// TODO: ideas - event for knocking
@SimpleComponent
@SimpleObject
public interface AccelerometerSensor extends SensorComponent {

  /**
   * Default AccelerationChanged event handler.
   * 
   * @param xAccel  acceleration minus Gx on the x-axis
   * @param yAccel  acceleration minus Gy on the y-axis
   * @param zAccel  acceleration minus Gz on the z-axis
   */
  @SimpleEvent
  void AccelerationChanged(float xAccel, float yAccel, float zAccel);

  /**
   * Default Shaking event handler.
   */
  @SimpleEvent
  void Shaking();

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that an accelerometer is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty
  boolean Available();

  /**
   * Enabled property getter method.
   * 
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty
  boolean Enabled();

  /**
   * Enabled property setter method.
   * 
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
                  initializer = "True")
  void Enabled(boolean enabled);

  /**
   * X acceleration property getter method (read-only property).
   * 
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return  x acceleration 
   */
  @SimpleProperty
  float XAccel();

  /**
   * Y acceleration property getter method (read-only property).
   * 
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return  y acceleration 
   */
  @SimpleProperty
  float YAccel();

  /**
   * Z acceleration property getter method (read-only property).
   * 
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return  z acceleration 
   */
  @SimpleProperty
  float ZAccel();
}
