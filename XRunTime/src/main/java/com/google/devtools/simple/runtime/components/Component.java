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

import com.google.devtools.simple.runtime.annotations.SimpleDataElement;
import com.google.devtools.simple.runtime.annotations.SimpleEvent;
import com.google.devtools.simple.runtime.annotations.SimpleObject;

/**
 * Simple component.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public interface Component {

  /*
   * Alignment constants.
   */
  @SimpleDataElement
  static final int HORIZONTAL_ALIGNMENT_LEFT = 0;
  @SimpleDataElement
  static final int HORIZONTAL_ALIGNMENT_CENTER = 1;
  @SimpleDataElement
  static final int HORIZONTAL_ALIGNMENT_RIGHT = 2;
  @SimpleDataElement
  static final int VERTICAL_ALIGNMENT_TOP = 0;
  @SimpleDataElement
  static final int VERTICAL_ALIGNMENT_CENTER = 1;
  @SimpleDataElement
  static final int VERTICAL_ALIGNMENT_BOTTOM = 2;

  /*
   * Color constants.
   */
  @SimpleDataElement
  static final int COLOR_NONE = 0x00FFFFFF;
  @SimpleDataElement
  static final int COLOR_BLACK = 0xFF000000;
  @SimpleDataElement
  static final int COLOR_BLUE = 0xFF0000FF;
  @SimpleDataElement
  static final int COLOR_CYAN = 0xFF00FFFF;
  @SimpleDataElement
  static final int COLOR_DKGRAY = 0xFF444444;
  @SimpleDataElement
  static final int COLOR_GRAY = 0xFF888888;
  @SimpleDataElement
  static final int COLOR_GREEN = 0xFF00FF00;
  @SimpleDataElement
  static final int COLOR_LTGRAY = 0xFFCCCCCC;
  @SimpleDataElement
  static final int COLOR_MAGENTA = 0xFFFF00FF;
  @SimpleDataElement
  static final int COLOR_RED = 0xFFFF0000;
  @SimpleDataElement
  static final int COLOR_WHITE = 0xFFFFFFFF;
  @SimpleDataElement
  static final int COLOR_YELLOW = 0xFFFFFF00;

  /*
   * Font constants.
   */
  @SimpleDataElement
  static final float FONT_DEFAULT_SIZE = 14;

  /*
   * Component gravity.
   */
  @SimpleDataElement
  static final float GRAVITY_CENTER = 0x11;
  @SimpleDataElement
  static final float GRAVITY_NORTH = 0x31;
  @SimpleDataElement
  static final float GRAVITY_NORTHWEST = 0x33;
  @SimpleDataElement
  static final float GRAVITY_WEST = 0x13;
  @SimpleDataElement
  static final float GRAVITY_SOUTHWEST = 0x53;
  @SimpleDataElement
  static final float GRAVITY_SOUTH = 0x51;
  @SimpleDataElement
  static final float GRAVITY_SOUTHEAST = 0x55;
  @SimpleDataElement
  static final float GRAVITY_EAST = 0x15;
  @SimpleDataElement
  static final float GRAVITY_NORTHEAST = 0x35;

  /*
   * Text justification constants.
   */
  @SimpleDataElement
  static final int JUSTIFY_LEFT = 0;
  @SimpleDataElement
  static final int JUSTIFY_CENTER = 1;
  @SimpleDataElement
  static final int JUSTIFY_RIGHT = 2;

  /*
   * Layout constants.
   */
  @SimpleDataElement
  static final int LAYOUT_LINEAR = 1;
  @SimpleDataElement
  static final int LAYOUT_TABLE = 2;
  @SimpleDataElement
  static final int LAYOUT_FRAME = 3;

  @SimpleDataElement
  static final int LAYOUT_ORIENTATION_HORIZONTAL = 0;
  @SimpleDataElement
  static final int LAYOUT_ORIENTATION_VERTICAL = 1;

  static final int LAYOUT_NO_COLUMN = -1;
  static final int LAYOUT_NO_ROW = -1;

  /*
   * Typeface constants.
   */
  @SimpleDataElement
  static final int TYPEFACE_DEFAULT = 0;
  @SimpleDataElement
  static final int TYPEFACE_SANSSERIF = 1;
  @SimpleDataElement
  static final int TYPEFACE_SERIF = 2;
  @SimpleDataElement
  static final int TYPEFACE_MONOSPACE = 3;
  
  /*
   * Length constants (for width and height).
   */
  @SimpleDataElement
  static final int LENGTH_PREFERRED = -1;
  @SimpleDataElement
  static final int LENGTH_FILL_PARENT = -2;

  /*
   * Touch event constants.
   */
  @SimpleDataElement
  static final int TOUCH_TAP = 0x00000000;
  @SimpleDataElement
  static final int TOUCH_DOUBLETAP = 0x00000001;
  @SimpleDataElement
  static final int TOUCH_FLINGUP = 0x00000002;
  @SimpleDataElement
  static final int TOUCH_FLINGDOWN = 0x00000003;
  @SimpleDataElement
  static final int TOUCH_FLINGLEFT = 0x00000004;
  @SimpleDataElement
  static final int TOUCH_FLINGRIGHT = 0x00000005;
  @SimpleDataElement
  static final int TOUCH_MOVEUP = 0x00000006;
  @SimpleDataElement
  static final int TOUCH_MOVEDOWN = 0x00000007;
  @SimpleDataElement
  static final int TOUCH_MOVELEFT = 0x00000008;
  @SimpleDataElement
  static final int TOUCH_MOVERIGHT = 0x00000009;

  /*
   * Keycode constants.
   */
  @SimpleDataElement
  static final int KEYCODE_0 = 0x00000007;
  @SimpleDataElement
  static final int KEYCODE_1 = 0x00000008;
  @SimpleDataElement
  static final int KEYCODE_2 = 0x00000009;
  @SimpleDataElement
  static final int KEYCODE_3 = 0x0000000A;
  @SimpleDataElement
  static final int KEYCODE_4 = 0x0000000B;
  @SimpleDataElement
  static final int KEYCODE_5 = 0x0000000C;
  @SimpleDataElement
  static final int KEYCODE_6 = 0x0000000D;
  @SimpleDataElement
  static final int KEYCODE_7 = 0x0000000E;
  @SimpleDataElement
  static final int KEYCODE_8 = 0x0000000F;
  @SimpleDataElement
  static final int KEYCODE_9 = 0x00000010;
  @SimpleDataElement
  static final int KEYCODE_A = 0x0000001D;
  @SimpleDataElement
  static final int KEYCODE_LEFT_ALT = 0x00000039;
  @SimpleDataElement
  static final int KEYCODE_RIGHT_ALT = 0x0000003A;
  @SimpleDataElement
  static final int KEYCODE_APOSTROPHE = 0x0000004B;
  @SimpleDataElement
  static final int KEYCODE_AT = 0x0000004D;
  @SimpleDataElement
  static final int KEYCODE_B = 0x0000001E;
  @SimpleDataElement
  static final int KEYCODE_BACK = 0x00000004;
  @SimpleDataElement
  static final int KEYCODE_BACKSLASH = 0x00000049;
  @SimpleDataElement
  static final int KEYCODE_C = 0x0000001F;
  @SimpleDataElement
  static final int KEYCODE_CALL = 0x00000005;
  @SimpleDataElement
  static final int KEYCODE_CAMERA = 0x0000001B;
  @SimpleDataElement
  static final int KEYCODE_CLEAR = 0x0000001C;
  @SimpleDataElement
  static final int KEYCODE_COMMA = 0x00000037;
  @SimpleDataElement
  static final int KEYCODE_D = 0x00000020;
  @SimpleDataElement
  static final int KEYCODE_DEL = 0x00000043;
  @SimpleDataElement
  static final int KEYCODE_PAD_CENTER = 0x00000017;
  @SimpleDataElement
  static final int KEYCODE_PAD_DOWN = 0x00000014;
  @SimpleDataElement
  static final int KEYCODE_PAD_LEFT = 0x00000015;
  @SimpleDataElement
  static final int KEYCODE_PAD_RIGHT = 0x00000016;
  @SimpleDataElement
  static final int KEYCODE_PAD_UP = 0x00000013;
  @SimpleDataElement
  static final int KEYCODE_E = 0x00000021;
  @SimpleDataElement
  static final int KEYCODE_ENDCALL = 0x00000006;
  @SimpleDataElement
  static final int KEYCODE_ENTER = 0x00000042;
  @SimpleDataElement
  static final int KEYCODE_ENVELOPE = 0x00000041;
  @SimpleDataElement
  static final int KEYCODE_EQUALS = 0x00000046;
  @SimpleDataElement
  static final int KEYCODE_EXPLORER = 0x00000040;
  @SimpleDataElement
  static final int KEYCODE_F = 0x00000022;
  @SimpleDataElement
  static final int KEYCODE_FOCUS = 0x00000050;
  @SimpleDataElement
  static final int KEYCODE_G = 0x00000023;
  @SimpleDataElement
  static final int KEYCODE_GRAVE = 0x00000044;
  @SimpleDataElement
  static final int KEYCODE_H = 0x00000024;
  @SimpleDataElement
  static final int KEYCODE_HEADSETHOOK = 0x0000004F;
  @SimpleDataElement
  static final int KEYCODE_HOME = 0x00000003;
  @SimpleDataElement
  static final int KEYCODE_I = 0x00000025;
  @SimpleDataElement
  static final int KEYCODE_J = 0x00000026;
  @SimpleDataElement
  static final int KEYCODE_K = 0x00000027;
  @SimpleDataElement
  static final int KEYCODE_L = 0x00000028;
  @SimpleDataElement
  static final int KEYCODE_LEFT_BRACKET = 0x00000047;
  @SimpleDataElement
  static final int KEYCODE_M = 0x00000029;
  @SimpleDataElement
  static final int KEYCODE_MEDIA_FAST_FORWARD = 0x0000005A;
  @SimpleDataElement
  static final int KEYCODE_MEDIA_NEXT = 0x00000057;
  @SimpleDataElement
  static final int KEYCODE_MEDIA_PLAY_PAUSE = 0x00000055;
  @SimpleDataElement
  static final int KEYCODE_MEDIA_PREVIOUS = 0x00000058;
  @SimpleDataElement
  static final int KEYCODE_MEDIA_REWIND = 0x00000059;
  @SimpleDataElement
  static final int KEYCODE_MEDIA_STOP = 0x00000056;
  @SimpleDataElement
  static final int KEYCODE_MENU = 0x00000052;
  @SimpleDataElement
  static final int KEYCODE_MINUS = 0x00000045;
  @SimpleDataElement
  static final int KEYCODE_MUTE = 0x0000005B;
  @SimpleDataElement
  static final int KEYCODE_N = 0x0000002A;
  @SimpleDataElement
  static final int KEYCODE_NOTIFICATION = 0x00000053;
  @SimpleDataElement
  static final int KEYCODE_NUM = 0x0000004E;
  @SimpleDataElement
  static final int KEYCODE_O = 0x0000002B;
  @SimpleDataElement
  static final int KEYCODE_P = 0x0000002C;
  @SimpleDataElement
  static final int KEYCODE_PERIOD = 0x00000038;
  @SimpleDataElement
  static final int KEYCODE_PLUS = 0x00000051;
  @SimpleDataElement
  static final int KEYCODE_POUND = 0x00000012;
  @SimpleDataElement
  static final int KEYCODE_POWER = 0x000000201A;
  @SimpleDataElement
  static final int KEYCODE_Q = 0x0000002D;
  @SimpleDataElement
  static final int KEYCODE_R = 0x0000002E;
  @SimpleDataElement
  static final int KEYCODE_RIGHT_BRACKET = 0x00000048;
  @SimpleDataElement
  static final int KEYCODE_S = 0x0000002F;
  @SimpleDataElement
  static final int KEYCODE_SEARCH = 0x00000054;
  @SimpleDataElement
  static final int KEYCODE_SEMICOLON = 0x0000004A;
  @SimpleDataElement
  static final int KEYCODE_LEFT_SHIFT = 0x0000003B;
  @SimpleDataElement
  static final int KEYCODE_RIGHT_SHIFT = 0x0000003C;
  @SimpleDataElement
  static final int KEYCODE_SLASH = 0x0000004C;
  @SimpleDataElement
  static final int KEYCODE_LEFT = 0x00000001;
  @SimpleDataElement
  static final int KEYCODE_RIGHT = 0x00000002;
  @SimpleDataElement
  static final int KEYCODE_SPACE = 0x0000003E;
  @SimpleDataElement
  static final int KEYCODE_STAR = 0x00000011;
  @SimpleDataElement
  static final int KEYCODE_SYM = 0x0000003F;
  @SimpleDataElement
  static final int KEYCODE_T = 0x00000030;
  @SimpleDataElement
  static final int KEYCODE_TAB = 0x0000003D;
  @SimpleDataElement
  static final int KEYCODE_U = 0x00000031;
  @SimpleDataElement
  static final int KEYCODE_V = 0x00000032;
  @SimpleDataElement
  static final int KEYCODE_VOLUME_DOWN = 0x00000019;
  @SimpleDataElement
  static final int KEYCODE_VOLUME_UP = 0x00000018;
  @SimpleDataElement
  static final int KEYCODE_W = 0x00000033;
  @SimpleDataElement
  static final int KEYCODE_X = 0x00000034;
  @SimpleDataElement
  static final int KEYCODE_Y = 0x00000035;
  @SimpleDataElement
  static final int KEYCODE_Z = 0x00000036;

  /**
   * Default Initialize event handler.
   */
  @SimpleEvent
  void Initialize();
}
