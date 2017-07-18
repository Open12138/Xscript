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
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;
import com.google.devtools.simple.runtime.annotations.UsesPermissions;

/**
 * Component for displaying images and animations.
 *
 * @author Herbert Czymontek
 */
@SimpleComponent
@SimpleObject
// This permission needed because the picture URL could reference a contact photo
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS")
public interface Image extends VisibleComponent {

  /**
   * Picture property setter method.
   *
   * @param imagePath  path and name of image
   */
  @SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_ASSET,
                  initializer = "\"\"")
  void Picture(String imagePath);
}
