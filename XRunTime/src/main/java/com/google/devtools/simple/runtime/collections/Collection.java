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

package com.google.devtools.simple.runtime.collections;

import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;
import com.google.devtools.simple.runtime.errors.IndexOutOfBoundsError;
import com.google.devtools.simple.runtime.variants.Variant;

import java.util.ArrayList;

/**
 * A collection is an ordered set of items. Unlike arrays where all members
 * must have the same data type, collections do not have that restriction.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public final class Collection {

  // Container for collection elements
  private ArrayList<Variant> list = new ArrayList<Variant>();

  /**
   * Removes all items from the collection.
   */
  @SimpleFunction
  public void Clear() {
    list.clear();
  }

  /**
   * Adds a new item to the collection.
   * 
   * @param item  item to be added  
   */
  @SimpleFunction
  public void Add(Variant item) {
    list.add(item);
  }

  /**
   * Returns the item at the specified position.
   * 
   * @param index  item position
   * @return  item
   */
  @SimpleFunction
  public Variant Item(int index) {
    try {
      return list.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsError();
    }
  }

  /**
   * Returns the number of items in the collection.
   * 
   * @return  number of items in the collection
   */
  @SimpleProperty
  public int Count() {
    return list.size();
  }

  /**
   * Checks whether an item is already part of the collection.
   * 
   * @param item  item to look for  
   * @return  {@code True} if the item is already in the collection
   */
  @SimpleFunction
  public boolean Contains(Variant item) {
    return list.contains(item);
  }

  /**
   * Removes an item from the collection.
   * 
   * @param item  item to remove
   */
  @SimpleFunction
  public void Remove(Variant item) {
    list.remove(item);
  }
}
