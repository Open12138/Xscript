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
import com.google.devtools.simple.runtime.components.Phone;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;

/**
 * Implementation of phone related functions.
 * 
 * @author Herbert Czymontek
 */
public final class PhoneImpl extends ComponentImpl implements Phone {

  private final Vibrator vibrator;

  /**
   * Creates a new Phone component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component, like this one
   *                   must be the form)
   */
  public PhoneImpl(ComponentContainer container) {
    super(container);

    vibrator = (Vibrator) ApplicationImpl.getContext().getSystemService(Context.VIBRATOR_SERVICE);
  }

  // Phone implementation

  @Override
  public boolean Available() {
    return true;
  }

  @Override
  public void Call(String phoneNumber) {
    if (null != phoneNumber && phoneNumber.length() > 0) {
      ApplicationImpl.getContext().startActivity(new Intent(Intent.ACTION_CALL,
          Uri.parse("tel:" + phoneNumber)));
    }
  }

  @Override
  public void Vibrate(int duration) {
    vibrator.vibrate(duration);
  }
}
