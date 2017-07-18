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
import com.google.devtools.simple.runtime.components.AccelerometerSensor;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Sensor that can detect shaking and measure acceleration in 3 dimensions.
 *
 * @author Herbert Czymontek
 */
public final class AccelerometerSensorImpl extends ComponentImpl
    implements AccelerometerSensor, SensorEventListener {

  // Skake threshold - derived by trial
  private final static double SHAKE_THRESHOLD = 8.0;

  // Cache for shake detection
  private final static int SENSOR_CACHE_SIZE = 10;
  private final Queue<Float> X_CACHE;
  private final Queue<Float> Y_CACHE;
  private final Queue<Float> Z_CACHE;

  // Backing for sensor values
  private float xAccel;
  private float yAccel;
  private float zAccel;

  // Accelerometer sensor
  private final Sensor sensor;

  // Indicates whether the timer is running or not
  private boolean enabled;

  /**
   * Creates a new OrientationSensor component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component, like this one
   *                   must be the form)
   */
  public AccelerometerSensorImpl(ComponentContainer container) {
    super(container);

    X_CACHE = new LinkedList<Float>();
    Y_CACHE = new LinkedList<Float>();
    Z_CACHE = new LinkedList<Float>();

    SensorManager sensors =
        (SensorManager) ApplicationImpl.getContext().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    if (sensor != null) {
      sensors.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }
  }

  // AccelerometerSensor implementation

  /**
   * {@inheritDoc}
   * 
   * <p>From the Android documentation:
   * <p>"All values are in SI units (m/s^2) and measure the acceleration applied
   * to the phone minus the force of gravity.
   * <br>values[0]: Acceleration minus Gx on the x-axis
   * <br>values[1]: Acceleration minus Gy on the y-axis
   * <br>values[2]: Acceleration minus Gz on the z-axis "
   */
  @Override
  public void AccelerationChanged(float xAccelNew, float yAccelNew, float zAccelNew) {
    xAccel = xAccelNew;
    yAccel = yAccelNew;
    zAccel = zAccelNew;

    addToSensorCache(X_CACHE, xAccelNew);
    addToSensorCache(Y_CACHE, yAccelNew);
    addToSensorCache(Z_CACHE, zAccelNew);

    if (isShaking(X_CACHE, xAccelNew) ||
        isShaking(Y_CACHE, yAccelNew) ||
        isShaking(Z_CACHE, zAccelNew)) {
      Shaking();
    }

    EventDispatcher.dispatchEvent(this, "AccelerationChanged", xAccelNew, yAccelNew, zAccelNew);
  }

  @Override
  public void Shaking() {
    EventDispatcher.dispatchEvent(this, "Shaking");
  }

  @Override
  public boolean Available() {
    return sensor != null;
//    List<Sensor> sensorList = sensors.getSensorList(Sensor.TYPE_ACCELEROMETER);
//    return sensorList != null && !sensorList.isEmpty();
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
  public float XAccel() {
    return xAccel;
  }

  @Override
  public float YAccel() {
    return yAccel;
  }

  @Override
  public float ZAccel() {
    return zAccel;
  }

  /*
   * Updating sensor cache, replacing oldest values.
   */
  private void addToSensorCache(Queue<Float> cache, float value) {
    if (cache.size() >= SENSOR_CACHE_SIZE) {
      cache.remove();
    }
    cache.add(value);
  }

  /*
   * Indicates whether there was a sudden, unusual movement.
   */
  // TODO: maybe this can be improved
  //       see http://www.utdallas.edu/~rxb023100/pubs/Accelerometer_WBSN.pdf 
  private boolean isShaking(Queue<Float> cache, float currentValue) {
    float average = 0;
    for (float value : cache) {
      average += value;
    }

    average /= cache.size();

    return Math.abs(average - currentValue) > SHAKE_THRESHOLD;
  }

  // SensorEventListener implementation
  
  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && enabled) {
      xAccel = event.values[0];
      yAccel = event.values[1];
      zAccel = event.values[2];
      AccelerationChanged(xAccel, yAccel, zAccel);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor s, int accuracy) {
  }
}
