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
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.OrientationSensor;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Sensor that can measure absolute orientation in 3 dimensions.
 *
 * @author Herbert Czymontek
 * @author Ellen Spertus
 */
public final class OrientationSensorImpl extends ComponentImpl
    implements OrientationSensor, SensorEventListener {

  private final SensorManager sensors;

  // Backing for properties
  private boolean enabled;
  private float yaw;
  private float pitch;
  private float roll;

  /**
   * Creates a new OrientationSensor component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component, like this one
   *                   must be the form)
   */
  public OrientationSensorImpl(ComponentContainer container) {
    super(container);

    sensors = (SensorManager) ApplicationImpl.getContext().getSystemService(Context.SENSOR_SERVICE);
    sensors.registerListener(this, sensors.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        SensorManager.SENSOR_DELAY_GAME);
  }

  // OrientationSensor implementation

  @Override
  public void OrientationChanged(float newYaw, float newPitch, float newRoll) {
    EventDispatcher.dispatchEvent(this, "OrientationChanged", newYaw, newPitch, newRoll);
  }

  @Override
  public boolean Available() {
    List<Sensor> sensorList = sensors.getSensorList(Sensor.TYPE_ORIENTATION);
    return sensorList != null && !sensorList.isEmpty();
  }

  @Override
  public boolean Enabled() {
    return enabled;
  }

  @Override
  public void Enabled(boolean enable) {
    enabled = enable;
  }

  @Override
  public float Pitch() {
    return pitch;
  }

  @Override
  public float Roll() {
    return roll;
  }

  @Override
  public float Yaw() {
    return yaw;
  }

  @Override
  public float Angle() {
    return (float) Math.toDegrees(Math.atan2(pitch, roll));
  }

  @Override
  public float Magnitude() {
    // Limit pitch and roll to 90; otherwise, the phone is upside down.
    // If the device is upside-down, it can range from -180 to 180.
    // We restrict it to the range [-90, 90].
    final int MAX_VALUE = 90;

    return (float) (Math.sqrt(Math.pow(Math.min(MAX_VALUE, pitch), 2) +
        Math.pow(Math.min(MAX_VALUE, roll), 2)) / (MAX_VALUE * Math.sqrt(2)));
  }

  // SensorEventListener implementation

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ORIENTATION && enabled) {
      yaw = event.values[0];
      pitch = event.values[1];
      roll = event.values[2];
      OrientationChanged(yaw, pitch, roll);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}
