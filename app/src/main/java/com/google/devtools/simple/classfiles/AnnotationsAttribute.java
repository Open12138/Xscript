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

package com.google.devtools.simple.classfiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Annotation attribute. 
 * 
 * <p>For more information about annotations see the Java VM specification.
 *
 * <p>Note that this implementation only supplies functionality required by
 * the Simple compiler.
 *
 * @author Herbert Czymontek
 */
public final class AnnotationsAttribute {

  public final class Annotation {

    // Constantpool index of annotation type
    private final short typeIndex; 

    /**
     * Defines a new annotation.
     * 
     * @param signature  annotation type
     */
    protected Annotation(String signature) {
      typeIndex = classFile.constantpool.newConstantUtf8(signature);
      attributeSize += 2 + 2;
    }

    /**
     * Generates binary data for the annotation into its class file's data
     * buffer.
     */
    protected void generate() {
      classFile.generate16(typeIndex);
      classFile.generate16((short) 0);
    }
  }

  // Class file
  private final ClassFile classFile;

  // Attribute name index
  private final short nameIndex;

  // Attribute size
  private int attributeSize;

  // Annotations
  private final List<Annotation> annotations;

  /**
   * Creates a new AnnotationsAttribute.
   *
   * @param name  annotation attribute name
   */
  AnnotationsAttribute(ClassFile classFile, String name) {
    this.classFile = classFile;

    nameIndex = classFile.constantpool.newConstantUtf8(name);
    attributeSize = 2;

    annotations = new ArrayList<Annotation>();
  }

  /**
   * Adds a new annotation to the attribute.
   * 
   * @param signature  annotation type signature
   * @return  new annotation
   */
  public Annotation newAnnotation(String signature) {
    Annotation annotation = new Annotation(signature); 
    annotations.add(annotation);
    return annotation;
  }

  /**
   * Returns the length of the attribute in the class file.
   * 
   * @return  length of attribute class file data
   */
  int getLength() {
    return 2 + 4 + attributeSize;
  }

  /**
   * Generates binary data for the attribute into its class file's data buffer.
   */ 
  protected void generate() {
    classFile.generate16(nameIndex);
    classFile.generate32(attributeSize);
    classFile.generate16((short) annotations.size());
    for (Annotation annotation : annotations) {
      annotation.generate();
    }
  }
}

