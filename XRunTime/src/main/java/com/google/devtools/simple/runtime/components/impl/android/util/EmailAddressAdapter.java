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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * EmailAddressAdapter provides email address completion from contacts, 
 * for use as a list adapter.
 *
 * <p>Note that most of this code was copied from 
 * com/google/android/gm/EmailAddressAdapter.java
 *
 * @author Sharon Perl
 */
public class EmailAddressAdapter extends ResourceCursorAdapter {
  public static final int NAME_INDEX = 1;
  public static final int DATA_INDEX = 2;

  private static final String SORT_ORDER = People.TIMES_CONTACTED + " DESC, " + People.NAME;

  private static final String[] PROJECTION = {
    ContactMethods._ID,     // 0
    ContactMethods.NAME,    // 1
    ContactMethods.DATA     // 2
  };

  private ContentResolver contentResolver;

  public EmailAddressAdapter(Context context) {
    super(context, android.R.layout.simple_dropdown_item_1line, null);

    contentResolver = context.getContentResolver();
  }

  @Override
  public final String convertToString(Cursor cursor) {
    String name = cursor.getString(NAME_INDEX);
    String address = cursor.getString(DATA_INDEX);

    return new Rfc822Token(name, address, null).toString();
  }

  private final String makeDisplayString(Cursor cursor) {
    String name = cursor.getString(NAME_INDEX);
    String address = cursor.getString(DATA_INDEX);

    return TextUtils.isEmpty(name) ? address : name + " <" + address + '>';
  }

  @Override
  public final void bindView(View view, Context context, Cursor cursor) {
    ((TextView) view).setText(makeDisplayString(cursor));
  }

  @Override
  public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
    String where = null;

    if (constraint != null) {
      String filter = DatabaseUtils.sqlEscapeString(constraint.toString() + '%');
      where = "(people.name LIKE " + filter + ") OR (contact_methods.data LIKE " + filter + ')';
    }

    return contentResolver.query(ContactMethods.CONTENT_EMAIL_URI, PROJECTION, where, null,
        SORT_ORDER);
  }
}
