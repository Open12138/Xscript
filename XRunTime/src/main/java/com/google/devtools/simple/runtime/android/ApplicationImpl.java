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

package com.google.devtools.simple.runtime.android;

import com.google.devtools.simple.runtime.Application;
import com.google.devtools.simple.runtime.ApplicationFunctions;
import com.google.devtools.simple.runtime.Files;
import com.google.devtools.simple.runtime.Log;
import com.google.devtools.simple.runtime.components.Component;
import com.google.devtools.simple.runtime.components.Form;
import com.google.devtools.simple.runtime.components.impl.android.FormImpl;
import com.google.devtools.simple.runtime.variants.StringVariant;
import com.google.devtools.simple.runtime.variants.Variant;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Component underlying activities and UI apps.
 *
 * <p>This is the root container of any Android activity and also the
 * superclass for for Simple/Android UI applications.
 *
 * @author Herbert Czymontek
 */
public final class ApplicationImpl extends Activity implements ApplicationFunctions {

  /**
   * Listener for distributing the Activity onResume() method to interested
   * components.
   */
  public interface OnResumeListener {
    public void onResume();
  }

  /**
   * Listener for distributing the Activity onStop() method to interested
   * components.
   */
  public interface OnStopListener {
    public void onStop();
  }

  // Activity context
  private static ApplicationImpl INSTANCE;

  // Activity resume and stop listeners
  private final List<OnResumeListener> onResumeListeners;
  private final List<OnStopListener> onStopListeners;

  // List with menu item captions
  private final List<String>  menuItems;

  // Touch gesture detector
  private GestureDetector gestureDetector;

  // Root view of application
  private ViewGroup rootView;

  // Content view of the application (lone child of root view)
  private View contentView;
  
  // Currently active form
  private FormImpl activeForm;

  /**
   * Returns the current activity context.
   *
   * @return  activity context
   */
  public static ApplicationImpl getContext() {
    return INSTANCE;
  }

  /**
   * Creates a new application.
   */
  public ApplicationImpl() {
    INSTANCE = this;

    menuItems = new ArrayList<String>();
    onResumeListeners = new ArrayList<OnResumeListener>();
    onStopListeners = new ArrayList<OnStopListener>();
  }

  @Override
  public void onCreate(Bundle icicle) {
    // Called when the activity is first created
    super.onCreate(icicle);

    gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int direction;
        int deltaX = (int) (e1.getRawX() - e2.getRawX());
        int deltaY = (int) (e1.getRawY() - e2.getRawY());

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
          // Horizontal move
          direction = deltaX > 0 ? Component.TOUCH_FLINGLEFT : Component.TOUCH_FLINGRIGHT;
        } else {
          // Vertical move
          direction = deltaY > 0 ? Component.TOUCH_FLINGUP : Component.TOUCH_FLINGDOWN;
        }

        if (activeForm != null) {
          activeForm.TouchGesture(direction);
        }
        return true;
      }

      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int direction;
        if (Math.abs(distanceX) > Math.abs(distanceY)) {
          // Horizontal move
          direction = distanceX > 0 ? Component.TOUCH_MOVELEFT : Component.TOUCH_MOVERIGHT;
        } else {
          // Vertical move
          direction = distanceY > 0 ? Component.TOUCH_MOVEUP : Component.TOUCH_MOVEDOWN;
        }

        if (activeForm != null) {
          activeForm.TouchGesture(direction);
        }
        return true;
      }

      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        if (activeForm != null) {
          activeForm.TouchGesture(Component.TOUCH_TAP);
        }
        return true;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        if (activeForm != null) {
          activeForm.TouchGesture(Component.TOUCH_DOUBLETAP);
        }
        return true;
      }
    });

    // Initialize runtime components
    Application.initialize(this);
    Log.initialize(new LogImpl(this));
    Files.initialize(getFilesDir());

    // We need to utilize a root view so that we can remove the actual form layout container
    // and re-add inside of a scrollable view. Otherwise there is no way removing the content view.
    rootView = new android.widget.FrameLayout(this);
    setContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        ViewGroup.LayoutParams.FILL_PARENT));

    // Get main form information and switch to it
    try {
      Bundle metaData = getPackageManager().getActivityInfo(getComponentName(),
          PackageManager.GET_META_DATA).metaData;

      String mainFormName =
          metaData.getString("com.google.devtools.simple.runtime.android.MainForm");
      Log.Info(Log.MODULE_NAME_RTL, "main form class: " + mainFormName);
      switchForm((FormImpl) getClassLoader().loadClass(mainFormName).newInstance());

    } catch (ClassNotFoundException e) {
      Log.Error(Log.MODULE_NAME_RTL, "main form class not found");
      finish();
    } catch (NameNotFoundException e) {
      Log.Error(Log.MODULE_NAME_RTL, "manifest file without main form data");
      finish();
    } catch (SecurityException e) {
      // Should not happen
      finish();
    } catch (InstantiationException e) {
      Log.Error(Log.MODULE_NAME_RTL, "failed to instantiate main form");
      finish();
    } catch (IllegalAccessException e) {
      // Should not happen
      finish();
    }
  }

  @Override
  public boolean onKeyDown(int keycode, KeyEvent event) {
    if (activeForm != null) {
      activeForm.Keyboard(keycode);
    }
    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    for (String caption : menuItems) {
      menu.add(caption);
    }
    return !menuItems.isEmpty();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (activeForm != null) {
      activeForm.MenuSelected(item.getTitle().toString());
    }
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gestureDetector.onTouchEvent(event);
  }

  @Override
  protected void onResume() {
    super.onResume();
    for (OnResumeListener onResumeListener : onResumeListeners) {
      onResumeListener.onResume();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    for (OnStopListener onStopListener : onStopListeners) {
      onStopListener.onStop();
    }
  }

  /**
   * Sets the given view as the content of the root view of the application
   *
   * @param view  new root view content
   */
  public void setContent(View view) {
    if (contentView != null) {
      rootView.removeView(contentView);
    }

    contentView = view;
    rootView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        ViewGroup.LayoutParams.FILL_PARENT));
  }

  /**
   * Checks whether the given form is the active form.
   *
   * @param form  form to check whether it is active
   * @return  {@code true} if the given form is active, {@code false} otherwise
   */
  public boolean isActiveForm(FormImpl form) {
    return form == activeForm;
  }

  /**
   * Adds the given listener to the onResume listeners.
   *
   * @param listener  listener to add
   */
  public void addOnResumeListener(OnResumeListener listener) {
    onResumeListeners.add(listener);
  }

  /**
   * Removes the given listener from the onResume listeners.
   *
   * @param listener  listener to remove
   */
  public void removeOnResumeListener(OnResumeListener listener) {
    onResumeListeners.remove(listener);
  }

  /**
   * Adds the given listener to the onStop listeners.
   *
   * @param listener  listener to add
   */
  public void addOnStopListener(OnStopListener listener) {
    onStopListeners.add(listener);
  }

  /**
   * Removes the given listener from the onStop listeners.
   *
   * @param listener  listener to remove
   */
  public void removeOnStopListener(OnStopListener listener) {
    onStopListeners.remove(listener);
  }

  // ApplicationFunctions implementation

  @Override
  public void addMenuItem(String caption) {
    menuItems.add(caption);
  }

  @Override
  public void switchForm(Form form) {
    FormImpl formImpl = (FormImpl) form;
    setContent(formImpl.getView());
    // Refresh title
    form.Title(form.Title());
    activeForm = formImpl;
  }

  @Override
  public Variant getPreference(String name) {
    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    return StringVariant.getStringVariant(preferences.getString(name, ""));
  }

  @Override
  public void storePreference(String name, Variant value) {
    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(name, value.getString());
    editor.commit();
  }
}
