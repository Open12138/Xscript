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

package com.google.devtools.simple.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;
import com.google.devtools.simple.runtime.variants.IntegerVariant;

/**
 * Helper class to deal with Simple object related stuff (like property
 * initialization).
 *
 * @author Herbert Czymontek
 */
public class Objects {

  private Objects() {
  }

  private static class PropertyDescriptor {
    private final PropertyInitializer pi;
    private final Method m;
    private final String initializer;

    PropertyDescriptor(PropertyInitializer pi, Method m, String initializer) {
      this.pi = pi;
      this.m = m;
      this.initializer = initializer;
    }

    void runInitializer(Object object) {
      pi.run(object, m, initializer);
    }
  }

  private static abstract class PropertyInitializer {
    void run(Object object, Method m, String value) {
      try {
        initializer(object, m, value);
      } catch (IllegalArgumentException e) {
        reportInitializePropertiesError(m, e);
      } catch (IllegalAccessException e) {
        // Should not happen
      } catch (InvocationTargetException e) {
        reportInitializePropertiesError(m, e);
      }
    }

    abstract void initializer(Object object, Method m, String value)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
    
    private static void reportInitializePropertiesError(Method m, Exception e) {
      Log.Error(Log.MODULE_NAME_RTL, "Runtime exception setting property default values: " +
          m.getName());
      e.printStackTrace();
    }
  }

  // Property initializer map
  private final static Map<String, PropertyInitializer> PROPERTY_INITIALIZERS =
      new HashMap<String, PropertyInitializer>();
  static {
    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_ASSET, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, unquote(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_BOOLEAN, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, value.equals("True"));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_COLOR, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Integer.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_DOUBLE, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Double.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
        new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Integer.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_INTEGER, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Integer.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_LAYOUT, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, IntegerVariant.getIntegerVariant(Integer.valueOf(value)));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_LONG, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Long.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_SINGLE, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Float.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_STRING, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, unquote(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_TEXT, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, value);
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_TEXTJUSTIFICATION,
        new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Integer.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_TYPEFACE, new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Integer.valueOf(value));
      }
    });

    PROPERTY_INITIALIZERS.put(SimpleProperty.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
        new PropertyInitializer() {
      @Override
      void initializer(Object object, Method m, String value)
          throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m.invoke(object, Integer.valueOf(value));
      }
    });
  }

  // Maps a component to its property initializers
  private static final Map<Class<?>, List<PropertyDescriptor>> COMPONENT_MAP =
      new HashMap<Class<?>, List<PropertyDescriptor>>();

  /**
   * Initializes properties with their default values as defined by their
   * {@code @SimpleProperty} declarations.
   *
   * <p>This method will be automatically invoked after the constructor for a
   * component finishes. It should never be invoked explicitly!
   *
   * @param object  Simple object instance whose properties must be initialized
   */
  public static void initializeProperties(Object object) {
    Class<?> cls = object.getClass();
    List<PropertyDescriptor> pdl = COMPONENT_MAP.get(cls);
    if (pdl == null) {
      pdl = new ArrayList<PropertyDescriptor>();
      initializeProperties(object, cls, pdl);
      COMPONENT_MAP.put(cls, pdl);
    } else {
      for (PropertyDescriptor pd : pdl) {
        pd.runInitializer(object);
      }
    }
  }

  /*
   * First time property initializer will collect all property initializers so that in subsequent
   * runs the component doesn't need to be introspected.
   */
  private static void initializeProperties(Object object, Class<?> cls,
      List<PropertyDescriptor> pdl) {
    Class<?> scls = cls.getSuperclass();
    if (scls != null && scls != Object.class) {
      initializeProperties(object, scls, pdl);
    }

    for (Class<?> iface : cls.getInterfaces()) {
      initializeProperties(object, iface, pdl);
    }

    SimpleObject c = cls.getAnnotation(SimpleObject.class);
    if (c != null) {
      for (Method m : cls.getDeclaredMethods()) {
        SimpleProperty p = m.getAnnotation(SimpleProperty.class);
        if (p != null) {
          String initializer = p.initializer();
          if (initializer.length() != 0) {
            if (!m.getReturnType().equals(Void.TYPE)) {
              Log.Warning(Log.MODULE_NAME_RTL, "ignoring initializer on property getter method: " +
                  cls.getName() + '.' + m.getName());
            } else {
              PropertyInitializer pi = PROPERTY_INITIALIZERS.get(p.type());
              if (pi == null) {
                // Ignoring unknown property types
                Log.Warning(Log.MODULE_NAME_RTL, "unknown property type: " + p.type());
              } else {
                PropertyDescriptor pd = new PropertyDescriptor(pi, m, initializer);
                pd.runInitializer(object);
                pdl.add(pd);
              }
            }
          }
        }
      }
    }
  }    

  private static String unquote(String s) {
    // Assumes quotes at the start and end of the given string
    return s.substring(1, s.length() - 1);
  }
}
