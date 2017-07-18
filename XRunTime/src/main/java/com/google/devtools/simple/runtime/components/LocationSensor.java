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
import com.google.devtools.simple.runtime.annotations.UsesPermissions;

/**
 * Sensor that can provide information on longitude, latitude, and altitude.
 *
 * @author Ellen Spertus
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames =
                 "android.permission.ACCESS_FINE_LOCATION," +
                 "android.permission.ACCESS_COARSE_LOCATION," +
                 "android.permission.ACCESS_MOCK_LOCATION," +
                 "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS")
public interface LocationSensor  extends SensorComponent {

  /**
   * Indicates that a new location has been detected.
   */
  @SimpleEvent
  void Changed(double latitude, double longitude, double altitude);

  /**
   * Indicates whether the device has a location sensor.
   */
  @SimpleProperty
  boolean IsAvailable();

  /**
   * Indicates whether altitude information is available.
   */
  @SimpleProperty
  boolean HasAltitude();

  /**
   * Indicates whether information about location accuracy is available.
   */
  @SimpleProperty
  boolean HasAccuracy();

  /**
   * The most recent available longitude value.  If no value is available,
   * 0 will be returned.
   */
  @SimpleProperty
  double Longitude();

  /**
   * The most recently available latitude value.  If no value is available,
   * 0 will be returned.
   */
  @SimpleProperty
  double Latitude();

  /**
   * The most recently available altitude value, in meters.  If no value is
   * available, 0 will be returned.
   */
  @SimpleProperty
  double Altitude();

  /**
   * The most recent measure of accuracy, in meters.  If no value is available,
   * 0 will be returned.
   */
  @SimpleProperty
  double Accuracy();

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
   * Provides a textual representation of the current address or
   * an empty string.
   */
  @SimpleProperty
  String CurrentAddress();
}
