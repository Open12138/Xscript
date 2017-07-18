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
import com.google.devtools.simple.runtime.components.TableLayout;
import com.google.devtools.simple.runtime.components.VisibleComponent;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TableRow;

/**
 * A layout component allowing subcomponents to be placed in tabular form.
 *
 * @author David Foster
 * @author Herbert Czymontek
 */
public class TableLayoutImpl extends LayoutImpl implements TableLayout {

  // Number of columns and rows of the table layout
  private int columns;
  private int rows;

  // Indicates that the table size cannot be changed any longer
  private boolean fixed;

  /**
   * Creates a new table layout.
   *
   * @param container  view container
   */
  TableLayoutImpl(ViewComponentContainer container) {
    super(new android.widget.TableLayout(ApplicationImpl.getContext()), container);

    android.widget.TableLayout layoutManager = (android.widget.TableLayout) getLayoutManager();
    layoutManager.setStretchAllColumns(true);
  }

  @Override
  public void Columns(int newColumns) {
    columns = newColumns;
  }

  @Override
  public void Rows(int newRows) {
    rows = newRows;
  }

  /**
   * {@inheritDoc}
   *
   * Note that table layout uses {@link #placeComponent(ViewComponent)} to
   * actually add the component. This is necessary because you need to set both
   * the {@link VisibleComponent#Column()} and {@link VisibleComponent#Row()}
   * properties before the component can be put into the layout.
   */
  @Override
  public void addComponent(ViewComponent component) {
  }

  /**
   * Places a component into its column and row.
   *
   * @param component  component ready to be placed into its column and row
   */
  void placeComponent(ViewComponent component) {
    ensureTableInitialized();

    int column = component.Column();
    int row = component.Row();
    if (0 <= column && column < columns && 0 <= row && row < rows) {
      ViewGroup layoutManager = getLayoutManager();
      ((TableRow) layoutManager.getChildAt(row)).addView(component.getView(),
          new TableRow.LayoutParams(column));
    }
  }

  private void ensureTableInitialized() {
    if (!fixed) {
      // After adding the first component, the table cannot be resized anymore
      fixed = true;

      ViewGroup layoutManager = getLayoutManager();
      Context context = layoutManager.getContext();
      for (int row = 0; row < rows; row++) {
         layoutManager.addView(new TableRow(context), new android.widget.TableLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
      }
    }
  }
}
