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

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Contacts;
import android.util.Log;

import java.io.IOException;

/**
 * Helper methods for dealing with images
 *
 * @author Sharon Perl
 */
public class ImageUtil {

  private ImageUtil() {
  }

  /**
   * Get and return a Drawable object from assets in the resources associated with
   * a view or from a contact photo.
   *
   * @param imagePath  path name for image. If it starts with "content:" it is
   *        assumed to be a contact photo. Otherwise, it is assumed to be an
   *        asset.
   * @param context  the activity context
   * @param placeholder_id the resource_id for the placeholder image resource
   * @param allow_contact_photos whether to allow path names to reference
   *        contact photos
   * @return Drawable object for the specified image, or {@code null} if
   *         parameter {@code imagePath} was the empty string or if a
   *         contact photo was requested but parameter
   *         {@code allow_contact_photos} was {@code false}
   * @throws IOException if the asset could not be found
   */
  public static Drawable getDrawable(String imagePath, Context context,
      int placeholder_id, boolean allow_contact_photos) throws IOException {
    if (imagePath.length() > 0) {
      if (imagePath.startsWith("content:")) {
        if (!allow_contact_photos) {
          // Contact photo requested but not permitted
          return null;
        }
        // Looks like a URI. Try to get the image from the contacts.
        Log.i("ImageUtil", "Trying to load contact photo for " + imagePath);
        return new BitmapDrawable(Contacts.People.loadContactPhoto(
            context, Uri.parse(imagePath), placeholder_id, null));
      }

      // Get the image from the Resources
      return Drawable.createFromStream(context.getResources().getAssets().open(imagePath),
          imagePath);
    }
    // imagePath was the empty string
    return null;
  }
}
