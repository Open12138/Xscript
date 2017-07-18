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
 * Sensor that can measure absolute orientation in 3 dimensions.
 *
 * @author Herbert Czymontek
 * @author Ellen Spertus
 */
// TODO: ideas - event for face up/down
@SimpleComponent
@SimpleObject
public interface OrientationSensor extends SensorComponent {

  /**
   * Default OrientationChanged event handler.
   *
   * @param yaw  angle between the magnetic north direction and the
   *             Y axis, around the Z axis (0 to 359). 0=North, 90=East,
   *             180=South, 270=West
   * @param pitch  rotation around X axis (-180 to 180), with positive
   *               values when the z-axis moves toward the y-axis.
   * @param roll  rotation around Y axis (-90 to 90), with positive
   *              values when the x-axis moves away from the z-axis.
   */
  @SimpleEvent
  void OrientationChanged(float yaw, float pitch, float roll);

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that an orientation sensor is available,
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
   * Pitch property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return  current pitch
   */
  @SimpleProperty
  float Pitch();

  /**
   * Roll property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return  current roll
   */
  @SimpleProperty
  float Roll();

  /**
   * Yaw property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return  current yaw
   */
  @SimpleProperty
  float Yaw();

  /**
   * Angle property getter method (read-only property). Specifically, this
   * provides the angle in which the orientation sensor is tilted, treating
   * {@link #Roll()} as the x-coordinate and {@link #Pitch()} as the
   * y-coordinate. For the magnitude of the tilt, use {@link #Magnitude()}.
   *
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return the angle in degrees
   */
  @SimpleProperty
  float Angle();

  /**
   * Magnitude property getter method (read-only property). Specifically, this
   * returns a number between 0 and 1, inclusive, indicating how far the device
   * is tilted. For the angle of tilt, use {@link #Angle()}.
   *
   * <p>To return meaningful values the sensor must be enabled.
   *
   * @return magnitude
   */
  @SimpleProperty
  float Magnitude();
}
