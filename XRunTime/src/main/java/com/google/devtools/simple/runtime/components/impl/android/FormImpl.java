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

import com.google.devtools.simple.runtime.Log;
import com.google.devtools.simple.runtime.android.ApplicationImpl;
import com.google.devtools.simple.runtime.components.Form;
import com.google.devtools.simple.runtime.errors.NoSuchFileError;
import com.google.devtools.simple.runtime.errors.PropertyAccessError;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.io.IOException;

/**
 * Implementation of form component.
 *
 * @author Herbert Czymontek
 */
public abstract class FormImpl extends PanelImpl implements Form {

  // Title property backing
  private String title;
  
  // Scroll view wrapper of form
  private ScrollView scroller;

  /**
   * Creates a new Form.
   */
  public FormImpl() {
    super(null);
  }

  // Form implementation

  @Override
  public void Keyboard(int keycode) {
    EventDispatcher.dispatchEvent(this, "Keyboard", keycode);
  }

  @Override
  public void MenuSelected(String caption) {
    EventDispatcher.dispatchEvent(this, "MenuSelected", caption);
  }

  @Override
  public void TouchGesture(int direction) {
    EventDispatcher.dispatchEvent(this, "TouchGesture", direction);
  }

  @Override
  public void BackgroundImage(String imagePath) {
    try {
      if (imagePath.length() != 0) {
        Activity context = ApplicationImpl.getContext();
        context.getWindow().setBackgroundDrawable(Drawable.createFromStream(
            context.getAssets().open(imagePath), imagePath));
      }
    } catch (IOException ioe) {
      throw new NoSuchFileError(imagePath);
    }
  }

  @Override
  public int Column() {
    throw new PropertyAccessError();
  }

  @Override
  public void Column(int newColumn) {
    Log.Warning(Log.MODULE_NAME_RTL, "attempt to set column for form component");
  }

  @Override
  public int Height() {
    return viewLayout.getLayoutManager().getHeight();
  }

  @Override
  public void Height(int newHeight) {
    throw new PropertyAccessError();
  }

  @Override
  public int Row() {
    throw new PropertyAccessError();
  }

  @Override
  public void Row(int newRow) {
    Log.Warning(Log.MODULE_NAME_RTL, "attempt to set row for form component");
  }

  @Override
  public boolean Scrollable() {
    return scroller != null;
  }

  @Override
  public void Scrollable(boolean scrollable) {
    ApplicationImpl app = ApplicationImpl.getContext();
    View view = viewLayout.getLayoutManager();

    if (scrollable) {
      if (scroller == null) {
        scroller = new ScrollView(app);
        if (app.isActiveForm(this)) {
          app.setContent(scroller);
        }
        scroller.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
            ViewGroup.LayoutParams.FILL_PARENT));
      }
    } else {
      if (scroller != null) {
        scroller.removeView(view);
        scroller = null;
        if (app.isActiveForm(this)) {
          app.setContent(view);
        }
      }
    }
  }

  @Override
  public String Title() {
    return title;
  }

  @Override
  public void Title(String newTitle) {
    title = newTitle;
    ApplicationImpl.getContext().setTitle(title);
  }

  @Override
  public int Width() {
    return viewLayout.getLayoutManager().getWidth();
  }

  @Override
  public void Width(int newWidth) {
    throw new PropertyAccessError();
  }

  @Override
  protected void addToContainer() {
    // Nothing to do
  }

  // ViewComponent implementation

  @Override
  public View getView() {
    return scroller != null ? scroller : viewLayout.getLayoutManager();
  }
}
