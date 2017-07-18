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

import com.google.devtools.simple.runtime.components.Component;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Helper methods for manipulating {@link View} objects.
 * 
 * @author David Foster
 * @author Herbert Czymontek
 */
public final class ViewUtil {

  private ViewUtil() {
  }
  
  /**
   * Returns the layout width of a {@link View}.
   * 
   * @param view   view instance
   * @return  layout width
   */
  public static int getWidth(View view) {
    return view.getWidth();
  }
  
  /**
   * Sets the layout width of a {@link View}.
   * 
   * @param view   view instance
   * @param width  layout width
   */
  public static void setWidth(View view, int width) {
    view.getLayoutParams().width = simpleToAndroidLength(width);
  }
  
  /**
   * Returns the layout height of a {@link View}.
   * 
   * @param view   view instance
   * @return  layout height
   */
  public static int getHeight(View view) {
    return view.getHeight();
  }
  
  /**
   * Sets the layout height of a {@link View}.
   *
   * @param view   view instance
   * @param height  layout height
   */
  public static void setHeight(View view, int height) {
    view.getLayoutParams().height = simpleToAndroidLength(height);
  }

  private static int simpleToAndroidLength(int simpleLength) {
    switch (simpleLength) {
      case Component.LENGTH_PREFERRED:
        return LayoutParams.WRAP_CONTENT;
      case Component.LENGTH_FILL_PARENT:
        return LayoutParams.FILL_PARENT;
      default:
        return simpleLength;
    }
  }
}
