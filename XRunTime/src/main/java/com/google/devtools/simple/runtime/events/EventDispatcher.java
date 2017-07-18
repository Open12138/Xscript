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

package com.google.devtools.simple.runtime.events;

import com.google.devtools.simple.runtime.errors.RuntimeError;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatches events to per object event handlers.
 * 
 * @author Herbert Czymontek
 */
public class EventDispatcher {

  /*
   * Simple event handlers associate a data member with an event handler in the class that defines
   * the data member. The reason we use reflection is so that we don't have to track assignments
   * to the data member.
   */
  private final static class EventHandlerClosure {
    // Object instance defining the data member
    private final Object object;

    // Data member associate with event handler
    private Field dataMember;

    // Event handler associated with data member
    private Method eventHandler;

    /**
     * Creates a new data object/event handler closure.
     * 
     * @param object  instance defining data member
     * @param dataMemberName  name of data member associated with event handler
     * @param eventName  name of event handler associated with data member
     */
    EventHandlerClosure(Object object, String dataMemberName, String eventName) {
      this.object = object;

      // Find event handler method (don't care about argument list - compiler checked at compile
      // time - and this way we don't have to propagate the information)
      Class<?> cls = object.getClass();
      for (Method method : cls.getMethods()) {
        if (method.getName().equals(dataMemberName + '$' + eventName)) {
          eventHandler = method;
          break;
        }
      }

      // Find data member
      try {
        dataMember = cls.getField(dataMemberName);
      } catch (NoSuchFieldException ignored) {
      }
    }

    /**
     * Get current object instance held by data member. 
     *
     * @return  data member content or {@code null}
     */
    Object getDataMemberObject() {
      if (dataMember == null) {
        return null;
      }
      try {
        return dataMember.get(object);
      } catch (IllegalAccessException ignored) {
        // Should never happen because all Simple data members are public
        return null;
      }
    }

    /**
     * Invokes the event handler.
     * 
     * @param args  actual event handler arguments
     */
    void invokeEvent(Object...args) {
      try {
        eventHandler.invoke(object, args);
      } catch (Throwable t) {
        RuntimeError.convertToRuntimeError(t);
      }
    }
  }

  // Mapping of event handler names to object/data member/event handler closures
  private static Map<String, List<EventHandlerClosure>> registry =
      new HashMap<String, List<EventHandlerClosure>>();

  private EventDispatcher() {
  }
  
  /**
   * Registers an object instance and its data member/event handler association
   * for event dispatching. Called from the constructor of object instance.
   * 
   * @param object  object instance defining data member and event handler
   * @param dataMemberName  name of data member associated with event handler
   * @param eventName  name of event associated with data member
   */
  public static void registerEvent(Object object, String dataMemberName, String eventName) {
    List<EventHandlerClosure> closures = registry.get(eventName);
    if (closures == null) {
      closures = new ArrayList<EventHandlerClosure>();
      registry.put(eventName, closures);
    }

    closures.add(new EventHandlerClosure(object, dataMemberName, eventName));
  }

  /**
   * Dispatches an event based on its name to any registered handlers.
   * 
   * @param that  object raising event (will have to match data member values)
   * @param eventName  name event raised
   * @param args  arguments to event handler
   */
  public static synchronized void dispatchEvent(Object that, String eventName, Object...args) {
    List<EventHandlerClosure> closures = registry.get(eventName);
    if (closures != null) {
      for (EventHandlerClosure closure :
          closures.toArray(new EventHandlerClosure[closures.size()])) {
        if (closure.getDataMemberObject() == that) {
          closure.invokeEvent(args);
        }
      }
    }
  }
}
