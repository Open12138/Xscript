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
import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;

/**
 * Simple Canvas component.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
public interface Canvas extends VisibleComponent {

  // Overriding default property values

  @Override
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_COLOR,
                  initializer = Component.COLOR_WHITE + "")
  void BackgroundColor(int argb);

  // Component declaration

  /**
   * Handler for touch events
   *
   * @param x  x-coordinate of the touched point
   * @param y  y-coordinate of the touched point
   */
  @SimpleEvent
  void Touched(int x, int y);

  /**
   * BackgroundImage property setter method: sets a background image for the
   * form.
   *
   * @param imagePath  path and name of image
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_ASSET,
                  initializer = "\"\"")
  void BackgroundImage(String imagePath);

  /**
   * PaintColor property getter method.
   *
   * @return  paint RGB color with alpha
   */
  @SimpleProperty
  int PaintColor();

  /**
   * PaintColor property setter method.
   *
   * @param argb  paint RGB color with alpha
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_COLOR,
                  initializer = Component.COLOR_BLACK + "")
  void PaintColor(int argb);

  /**
   * Clears the canvas (fills it with the background color).
   */
  @SimpleFunction
  void Clear();

  /**
   * Draws a point at the given coordinates on the canvas.
   *
   * @param x  x coordinate
   * @param y  y coordinate
   */
  @SimpleFunction
  void DrawPoint(int x, int y);

  /**
   * Draws a circle at the given coordinates on the canvas, with the given radius
   *
   * @param x  x coordinate
   * @param y  y coordinate
   * @param r  radius
   */
  @SimpleFunction
  void DrawCircle(int x, int y, float r);

  /**
   * Draws a line between the given coordinates on the canvas.
   *
   * @param x1  x coordinate of first point
   * @param y1  y coordinate of first point
   * @param x2  x coordinate of second point
   * @param y2  y coordinate of second point
   */
  @SimpleFunction
  void DrawLine(int x1, int y1, int x2, int y2);

  // TODO: add more drawing primitives
}
