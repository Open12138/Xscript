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

import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;

/**
 * Visible Simple component.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public interface VisibleComponent extends Component {

  /**
   * BackgroundColor property getter method.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty
  int BackgroundColor();

  /**
   * BackgroundColor property setter method.
   *
   * @param argb  background RGB color with alpha
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_COLOR)
  void BackgroundColor(int argb);

  /**
   * Column property getter method.
   * 
   * @return  column used by the table layout
   */
  @SimpleProperty
  int Column();
  
  /**
   * Column property setter method.
   * 
   * @param column  column used by the table layout
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_INTEGER,
                  initializer = Component.LAYOUT_NO_COLUMN + "")
  void Column(int column);

  /**
   * Height property getter method.
   * 
   * @return  height property used by the layout
   */
  @SimpleProperty
  int Height();
  
  /**
   * Height property setter method.
   * 
   * @param height  height property used by the layout
   */
  void Height(int height);
  
  /**
   * Row property getter method.
   * 
   * @return  row used by the table layout
   */
  @SimpleProperty
  int Row();
  
  /**
   * Row property setter method.
   * 
   * @param row  row used by the table layout
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_INTEGER,
                  initializer = Component.LAYOUT_NO_ROW + "")
  void Row(int row);
  
  /**
   * Width property getter method.
   * 
   * @return  width property used by the layout
   */
  @SimpleProperty
  int Width();
  
  /**
   * Width property setter method.
   * 
   * @param width  width property used by the layout
   */
  @SimpleProperty
  void Width(int width);
}
