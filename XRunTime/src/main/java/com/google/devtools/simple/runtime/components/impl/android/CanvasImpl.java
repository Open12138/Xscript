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
import com.google.devtools.simple.runtime.components.Canvas;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.impl.android.util.PaintUtil;
import com.google.devtools.simple.runtime.errors.NoSuchFileError;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

/**
 * Android implementation of Simple Canvas component.
 *
 * @author Herbert Czymontek
 */
public final class CanvasImpl extends ViewComponent implements Canvas {
  /**
   * Panel that can be drawn on. This overrides
   * {@link View#onDraw(android.graphics.Canvas)} but not
   * {@link View#onTouchEvent(MotionEvent)},
   * which should be overridden in any subclass that should handle touch events.
   */
  private class CanvasView extends View {
    protected final android.graphics.Canvas canvas;
    protected final Bitmap bitmap;

    public CanvasView(Context context) {
      super(context);

      bitmap = Bitmap.createBitmap(context.getWallpaperDesiredMinimumWidth(),
          context.getWallpaperDesiredMinimumWidth(), Bitmap.Config.ARGB_8888);
      canvas = new android.graphics.Canvas(bitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      // touch event also have pressure and size but we're not using them
      Touched((int) event.getX(), (int) event.getY());
      return true;
    }

    @Override
    protected void onDraw(android.graphics.Canvas c) {
      if (bitmap != null) {
        c.drawBitmap(bitmap, 0, 0, null);
      }
    }
  }

  // Colors
  private Paint paintColor;
  private Paint backgroundColor;

  /**
   * Creates a new Canvas component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}
   */
  public CanvasImpl(ComponentContainer container) {
    super(container);
  }
  
  @Override
  protected View createView() {
    CanvasView view = new CanvasView(ApplicationImpl.getContext());

    // Initialize colors
    backgroundColor = new Paint();
    paintColor = new Paint();
    paintColor.setStrokeWidth(1);

    return view;
  }

  // Canvas implementation

  @Override
  public void Touched(int x, int y) {
    EventDispatcher.dispatchEvent(this, "Touched", x, y);
  }

  @Override
  public int BackgroundColor() {
    return PaintUtil.extractARGB(backgroundColor);
  }

  @Override
  public void BackgroundColor(int argb) {
    PaintUtil.changePaint(backgroundColor, argb);
    CanvasView view = (CanvasView) getView();
    view.canvas.drawPaint(backgroundColor);
    view.invalidate();
  }

  @Override
  public void BackgroundImage(String imagePath) {
    try {
      if (imagePath.length() > 0) {
        CanvasView view = (CanvasView) getView();
        view.setBackgroundDrawable(Drawable.createFromStream(
            view.getContext().getAssets().open(imagePath), imagePath));
      }
    } catch (IOException ioe) {
      throw new NoSuchFileError(imagePath);
    }
  }

  @Override
  public int PaintColor() {
    return PaintUtil.extractARGB(paintColor);
  }

  @Override
  public void PaintColor(int argb) {
    PaintUtil.changePaint(paintColor, argb);
  }

  @Override
  public void Clear() {
    CanvasView view = (CanvasView) getView();
    view.bitmap.eraseColor(Color.TRANSPARENT);
    view.invalidate();
  }

  @Override
  public void DrawPoint(int x, int y) {
    CanvasView view = (CanvasView) getView();
    view.canvas.drawPoint(x, y, paintColor);
    view.invalidate();
  }

  @Override
  public void DrawCircle(int x, int y, float r) {
    CanvasView view = (CanvasView) getView();
    view.canvas.drawCircle(x, y, r, paintColor);
    view.invalidate();
  }

  @Override
  public void DrawLine(int x1, int y1, int x2, int y2) {
    CanvasView view = (CanvasView) getView();
    view.canvas.drawLine(x1, y1, x2, y2, paintColor);
    view.invalidate();
  }
}
