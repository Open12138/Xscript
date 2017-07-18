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
import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;
import com.google.devtools.simple.runtime.annotations.UsesPermissions;

/**
 * Simple Phone component.
 *
 * <p>Allows access to phone functionality.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.CALL_PHONE")
public interface Phone extends Component {

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that phone functionality is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty
  boolean Available();

  /**
   * Places a call to the given phone number.
   *
   * @param phoneNumber  phone number in the form of numbers only (no spaces,
   *                     no dashes etc.)
   */
  @SimpleFunction
  void Call(String phoneNumber);

  /**
   * Vibrates the phone.
   *
   * @param duration  duration in milliseconds
   */
  @SimpleFunction
  void Vibrate(int duration);
}
