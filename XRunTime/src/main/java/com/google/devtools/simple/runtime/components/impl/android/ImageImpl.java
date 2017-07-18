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
import com.google.devtools.simple.runtime.components.Image;
import com.google.devtools.simple.runtime.components.impl.android.util.ImageUtil;
import com.google.devtools.simple.runtime.errors.NoSuchFileError;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Component for displaying images and animations.
 *
 * @author Herbert Czymontek
 */
public final class ImageImpl extends ViewComponent implements Image {

  // Backing for background color
  private int backgroundColor;

  /**
   * Creates a new Image component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public ImageImpl(ComponentContainer container) {
    super(container);
  }

  @Override
  protected View createView() {
    View view = new ImageView(ApplicationImpl.getContext()) {
      @Override
      public boolean verifyDrawable(Drawable dr) {
        super.verifyDrawable(dr);
        return true;
      }
    };

    // Adds the component to its designated container
    view.setFocusable(true);

    return view;
  }

  // Image implementation

  @Override
  public int BackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    ImageView view = (ImageView) getView();
    view.setBackgroundColor(argb);
    view.invalidate();
  }

  @Override
  public void Picture(String imagePath) {
    try {
      Drawable drawable = ImageUtil.getDrawable(imagePath, ApplicationImpl.getContext(),
          android.R.drawable.picture_frame, true);
      if (drawable != null) {
        ImageView view = (ImageView) getView();
        view.setImageDrawable(drawable);
        view.setAdjustViewBounds(true);
      }
    } catch (IOException ioe) {
      throw new NoSuchFileError(imagePath);
    }
  }
}
