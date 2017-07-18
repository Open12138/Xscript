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
import com.google.devtools.simple.runtime.events.EventDispatcher;
import com.google.devtools.simple.runtime.components.Timer;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;

import android.os.Handler;

/**
 * Android implementation of Simple Timer component.
 *
 * @author Herbert Czymontek
 */
public final class TimerImpl extends ComponentImpl implements Timer, Runnable {

  // Android message handler used as a timer
  private Handler handler;

  // Indicates whether the timer is running or not
  private boolean enabled;

  // Interval between timer events in ms
  private int interval;

  /**
   * Creates a new Timer component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component, like this one
   *                   must be the form)
   */
  public TimerImpl(ComponentContainer container) {
    super(container);

    handler = new Handler();
  }

  @Override
  public void Timer() {
    EventDispatcher.dispatchEvent(this, "Timer");
  }

  @Override
  public int Interval() {
    return interval;
  }

  @Override
  public void Interval(int newInterval) {
    interval = newInterval;
    if (enabled) {
      handler.removeCallbacks(this);
      handler.postDelayed(this, newInterval);
    }
  }

  @Override
  public boolean Enabled() {
    return enabled;
  }

  @Override
  public void Enabled(boolean enable) {
    if (enabled) {
      handler.removeCallbacks(this);
    }

    enabled = enable;

    if (enable) {
      handler.postDelayed(this, interval);
    }
  }

  // Runnable implementation

  public void run() {
    if (enabled) {
      Timer();
      handler.postDelayed(this, interval);
    }
  }
}
