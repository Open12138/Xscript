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

import com.google.devtools.simple.util.Preconditions;

/**
 * Representation of a field in a class file.
 * 
 * <p>For more information about field descriptors see the Java VM
 * specification.
 *
 * @author Herbert Czymontek
 */
public final class Field {
  /**
   * Field access flags.
   */
  public static final short ACC_PUBLIC = 0x0001;
  public static final short ACC_PRIVATE = 0x0002;
  public static final short ACC_PROTECTED = 0x0004;
  public static final short ACC_STATIC = 0x0008;
  public static final short ACC_FINAL = 0x0010;
  public static final short ACC_VOLATILE = 0x0040;
  public static final short ACC_TRANSIENT = 0x0080;
  public static final short ACC_SYNTHETIC = 0x1000;
  public static final short ACC_ENUM = 0x4000;

  // Mask containing all valid field access flags
  private static final short ALLOWED_ACC_FLAGS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED |
      ACC_STATIC | ACC_FINAL | ACC_VOLATILE | ACC_TRANSIENT | ACC_SYNTHETIC | ACC_ENUM;

  // Class file defining this field
  private final ClassFile classFile;

  // Constant pool indices of field name and signature
  private final short nameIndex;
  private final short signatureIndex;

  // Access flags for field
  private final short flags;

  // Field attribute count
  private short attributeCount;

  // Constant pool index of ConstantValue attribute name (0 if not used)
  private short constantValueAttributeIndex;

  // Constant pool index for fields defining a constant (0 if not used)
  private short constantIndex;

  // Runtime visible annotations
  private AnnotationsAttribute runtimeVisibleAnnotationsAttribute;

  /**
   * Defines a new field for a class file.
   * 
   * @param classFile  class file containing the field
   * @param flags  field access flags
   * @param name  field name
   * @param signature  field signature
   */
  protected Field(ClassFile classFile, short flags, String name, String signature) {
    Preconditions.checkArgument((flags & ALLOWED_ACC_FLAGS) == flags);

    this.flags = flags;
    this.classFile = classFile;
    nameIndex = classFile.constantpool.newConstantUtf8(name);
    signatureIndex = classFile.constantpool.newConstantUtf8(signature);
  }

  /**
   * Creates a constant pool index for the ConstantValue attribute name.
   * Because this index is initialized lazily, it is important that
   * fields creating ConstantValue attributes must call this method.
   */
  protected void createConstantValueAttributeIndex() {
    Preconditions.checkState(constantValueAttributeIndex == 0);

    constantValueAttributeIndex = classFile.constantpool.newConstantUtf8("ConstantValue");
    attributeCount++;
  }

  /**
   * Associates a constant int value with this field.
   * 
   * @param value  constant int value
   */
  public void setConstantIntegerValue(int value) {
    createConstantValueAttributeIndex();
    constantIndex = classFile.constantpool.newConstantInteger(value);
  }

  /**
   * Associates a constant float value with this field.
   * 
   * @param value  constant float value
   */
  public void setConstantFloatValue(float value) {
    createConstantValueAttributeIndex();
    constantIndex = classFile.constantpool.newConstantFloat(value);
  }

  /**
   * Associates a constant long value with this field.
   * 
   * @param value  constant long value
   */
  public void setConstantLongValue(long value) {
    createConstantValueAttributeIndex();
    constantIndex = classFile.constantpool.newConstantLong(value);
  }

  /**
   * Associates a constant double value with this field.
   * 
   * @param value  constant double value
   */
  public void setConstantDoubleValue(double value) {
    createConstantValueAttributeIndex();
    constantIndex = classFile.constantpool.newConstantDouble(value);
  }

  /**
   * Associates a constant string value with this field.
   * 
   * @param value  constant string value
   */
  public void setConstantStringValue(String value) {
    createConstantValueAttributeIndex();
    constantIndex = classFile.constantpool.newConstantString(value);
  }

  /**
   * Returns runtime visible annotations attribute. 
   * 
   * @return  runtime visible annotations attribute
   */
  public AnnotationsAttribute getRuntimeVisibleAnnotationsAttribute() {
    if (runtimeVisibleAnnotationsAttribute == null) {
      runtimeVisibleAnnotationsAttribute =
          new AnnotationsAttribute(classFile, "RuntimeVisibleAnnotations");
      attributeCount++;
    }

    return runtimeVisibleAnnotationsAttribute;
  }

  /**
   * Generates binary data for the field into its class file's data buffer.
   */
  protected void generate() {
    classFile.generate16(flags);
    classFile.generate16(nameIndex);
    classFile.generate16(signatureIndex);

    classFile.generate16(attributeCount);

      // ConstantValue attribute
    if (constantValueAttributeIndex != 0) {
      classFile.generate16(constantValueAttributeIndex);
      classFile.generate32(2);
      classFile.generate16(constantIndex);
    }

    // RuntimeVisibleAnnotations attribute
    if (runtimeVisibleAnnotationsAttribute != null) {
      runtimeVisibleAnnotationsAttribute.generate();
    }
  }
}
