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

package com.google.devtools.simple.runtime.components.impl.android.util;

import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;

/**
 * Functionality related to {@link android.graphics.Color} and
 * {@link Paint}.
 *
 * @author Ellen Spertus
 */
public class PaintUtil {
  private PaintUtil() {}

  /**
   * Extracts an ARGB (alpha-red-green-blue) value from a Paint.
   *
   * @param paint the source of the ARGB values
   * @return a 32-bit integer which eight bits for alpha, red, green, and blue,
   *         respectively
   */
  public static int extractARGB(Paint paint) {
    return paint.getColor() | (paint.getAlpha() << 24);
  }

  /**
   * Changes the paint color the specified value.
   *
   * @param paint  the object to mutate with the new color
   * @param argb  a 32-bit integer with eight bits for alpha, red, green, and
   *              blue, respectively; the value 0 has the special meaning of
   *              erasing points
   */
  public static void changePaint(Paint paint, int argb) {
    if ((argb & 0xFF000000) == 0) {
      paint.setAlpha(0x00);
      paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
    } else {
      paint.setColor(argb & 0x00FFFFFF);
      paint.setAlpha((argb >> 24) & 0xFF);
      paint.setXfermode(null);
    }
  }
}
