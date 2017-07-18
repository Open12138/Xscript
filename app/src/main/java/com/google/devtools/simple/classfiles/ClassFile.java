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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Java class file.
 * 
 * <p>For more information about Java class files see the Java VM
 * specification.
 *
 * @author Herbert Czymontek
 */
public final class ClassFile {

  /*
   * Represents an inner class attribute as defined by the Java VM specification.
   */
  private class InnerClassAttribute {
    // Indices into the constant pool
    private final short innerclassIndex;
    private final short outerclassIndex;
    private final short nameIndex;
    private final short innerFlags;

    /*
     * Creates a new inner class attribute.
     */
    InnerClassAttribute(short innerclassIndex, short outerclassIndex, short nameIndex,
        short innerFlags) {
      this.innerclassIndex = innerclassIndex;
      this.outerclassIndex = outerclassIndex;
      this.nameIndex = nameIndex;
      this.innerFlags = innerFlags;
    }

    /*
     * Writes the inner class attribute into the classes byte buffer.
     */
    void generate() {
      generate16(innerclassIndex);
      generate16(outerclassIndex);
      generate16(nameIndex);
      generate16(innerFlags);
    }
  }

  /**
   * Class access flags. 
   */
  public static final short ACC_PUBLIC = 0x0001;
  public static final short ACC_FINAL = 0x0010;
  public static final short ACC_SUPER = 0x0020;
  public static final short ACC_INTERFACE = 0x0200;
  public static final short ACC_ABSTRACT = 0x0400;
  public static final short ACC_SYNTHETIC = 0x1000;
  public static final short ACC_ANNOTATION = 0x2000;
  public static final short ACC_ENUM = 0x4000;

  // Mask containing all valid class access flags
  private static final short ALLOWED_ACC_FLAGS = ACC_PUBLIC | ACC_FINAL | ACC_SUPER |
      ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC | ACC_ANNOTATION | ACC_ENUM;

  // Byte buffer holding the binary representation of the class
  private Buffer buffer;

  // Internal name of class
  private final String internalName;

  // Class constant pool
  protected final ConstantPool constantpool;

  // Access flags
  private final short flags;

  // Constant pool index of this class
  private final short thisclass;

  // Constant pool index of superclass
  private final short superclass;

  // List of constant pool indices of implemented interfaces by the class
  private final List<Short> interfaces;

  // List of fields defined by this class
  private final List<Field> fields;

  // List of methods defined by this class
  private final List<Method> methods;

  // List of required inner class attributes
  private final List<InnerClassAttribute> innerclasses;

  // Runtime visible annotations
  private AnnotationsAttribute runtimeVisibleAnnotationsAttribute;

  // Constant pool index of source file
  private short sourceFileIndex;

  // Class file attribute count
  private short attributeCount;

  // Constant pool index of InnerClasses attribute name (initialized lazily)
  private short innerClassesAttributeIndex;

  // Constant pool index of SourceFile attribute name (initialized lazily)
  private short sourceFileAttributeIndex;

  // Constant pool index of LocalVariableTable attribute name (initialized lazily)
  private short localVariableTableAttributeIndex;

  /**
   * Creates a new instance of ClassFile.
   * 
   * @param flags  class access flags
   * @param internalName  internal name of class
   * @param superclassInternalName  internal name of superclass
   */
  public ClassFile(short flags, String internalName, String superclassInternalName) {
    constantpool = new ConstantPool();
    interfaces = new ArrayList<Short>();
    fields = new ArrayList<Field>();
    methods = new ArrayList<Method>();
    innerclasses = new ArrayList<InnerClassAttribute>();

    Preconditions.checkArgument((flags & ALLOWED_ACC_FLAGS) == flags);

    this.flags = flags;
    this.internalName = internalName;
    thisclass = constantpool.newConstantClass(internalName);
    superclass = constantpool.newConstantClass(superclassInternalName);
  }

  /**
   * Returns the internal name of the class.
   * 
   * @return  internal name
   */
  public String getInternalName() {
    return internalName;
  }

  /**
   * Returns the name of the class.
   * 
   * @return  name
   */
  public String getName() {
    return internalName.replace('/', '.');
  }

  /**
   * Adds the given internal class name to the list of interfaces implemented
   * by this class.
   * 
   * @param internalInterfaceName  class name in internal format
   */
  public void addInterfaceImplementation(String internalInterfaceName) {
    interfaces.add(Short.valueOf(constantpool.newConstantClass(internalInterfaceName)));
  }

  /**
   * Adds a new method defined by this class.
   * 
   * @param access  method access flags
   * @param name  method name
   * @param signature  method signature
   * @return  {@link Method} object for generating class file data for the
   *          method
   */
  public Method newMethod(short access, String name, String signature) {
    Method method = new Method(this, access, name, signature);
    methods.add(method);
    return method;
  }

  /**
   * Adds a new field defined by this class.
   * 
   * @param access  field access flags
   * @param name  field name
   * @param signature  field signature
   * @return  {@link Field} object for generating class file data for the
   *          field
   */
  public Field newField(short access, String name, String signature) {
    Field field = new Field(this, access, name, signature);
    fields.add(field);
    return field;
  }

  /**
   * Adds a new inner class defined by this class.
   * 
   * @param access  inner class access flags
   * @param name  inner class names
   * @param innerInternalname  name of inner class in internal format
   */
  public void addInnerClass(short access, String name, String innerInternalname) {
    createInnerClassesAttributeIndex();
    short innerclassIndex = constantpool.newConstantClass(innerInternalname);
    short nameIndex = name == null ? (short) 0 : constantpool.newConstantUtf8(name);

    innerclasses.add(new InnerClassAttribute(innerclassIndex, thisclass, nameIndex, access));
  }

  /**
   * Declares a class as the outer class of this class.
   * 
   * @param access  outer class access flags
   * @param name  outer class names
   * @param outerInternalname  name of outer class in internal format
   */
  public void addOuterClass(short access, String name, String outerInternalname) {
    createInnerClassesAttributeIndex();
    short outerclassIndex = constantpool.newConstantClass(outerInternalname);
    short nameIndex = name == null ? (short) 0 : constantpool.newConstantUtf8(name);

    innerclasses.add(new InnerClassAttribute(thisclass, outerclassIndex, nameIndex, access));
  }

  /**
   * Returns runtime visible annotations attribute. 
   * 
   * @return  runtime visible annotations attribute
   */
  public AnnotationsAttribute getRuntimeVisibleAnnotationsAttribute() {
    if (runtimeVisibleAnnotationsAttribute == null) {
      runtimeVisibleAnnotationsAttribute =
          new AnnotationsAttribute(this, "RuntimeVisibleAnnotations");
      attributeCount++;
    }

    return runtimeVisibleAnnotationsAttribute;
  }
  
  /**
   * Sets the name of the corresponding source file.
   * 
   * @param filename  filename of source file without any directory information
   */
  public void setSourceFile(String filename) {
    createSourceFileAttributeIndex();
    sourceFileIndex = constantpool.newConstantUtf8(filename);
  }
  
  /**
   * Generates the binary data for the class file. For detailed information
   * about Java class files see the Java VM specification.
   * 
   * @return  byte array containing binary data for class file
   */
  public byte[] generate() {
    buffer = new Buffer();

    // Generate magic number and classfile version information
    generate32(0xCAFEBABE);
    generate16((short) 0); // Minor version
    generate16((short) (44 + 5)); // Major version - JDK 1.5 for now

    // Generate constant pool
    constantpool.generate(this);

    // Generate class access flags
    generate16(flags);

    // Generate class hierarchy information
    generate16(thisclass);
    generate16(superclass);

    // Generate attribute for implemented interfaces
    generate16((short) interfaces.size());
    for (Short s : interfaces) {
      generate16(s);
    }

    // Generate field information
    generate16((short) fields.size());
    for (Field field : fields) {
      field.generate();
    }

    // Generate method information
    generate16((short) methods.size());
    for (Method method : methods) {
      method.generate();
    }

    // Generate attribute count
    generate16(attributeCount);
    
    // Generate InnerClasses attribute
    if (innerclasses.size() != 0) {
      generateInnerclassAttribute();
    }

    // Generate RuntimeVisibleAnnotations attribute
    if (runtimeVisibleAnnotationsAttribute != null) {
      runtimeVisibleAnnotationsAttribute.generate();
    }
    
    // Generate SourceFile attribute
    if (sourceFileIndex != 0) {
      generateSourceFileAttribute();
    }

    return buffer.toByteArray();
  }

  /**
   * Generates a byte value into the class data buffer.
   * 
   * @param b  byte to generate
   */
  protected void generate8(byte b) {
    buffer.generate8(b);
  }

  /**
   * Generates a short value into the class data buffer.
   * 
   * @param s  short to generate
   */
  protected void generate16(short s) {
    buffer.generate16(s);
  }

  /**
   * Generates an int value into the class data buffer.
   * 
   * @param i  int to generate
   */
  protected void generate32(int i) {
    buffer.generate32(i);
  }

  /**
   * Generates a long value into the class data buffer.
   * 
   * @param l  long to generate
   */
  protected void generate64(long l) {
    buffer.generate64(l);
  }

  /**
   * Copies an array of bytes into the class data buffer.
   * 
   * @param bytes  bytes to copy
   */
  protected void generateBytes(byte[] bytes) {
    buffer.generateBytes(bytes);
  }

  /**
   * Creates a constant pool index for the LocalVariableTable attribute name.
   * Because this index is initialized lazily, it is important that
   * methods creating LocalVariableTable attributes must call this method.
   */
  protected void createLocalVariableTableAttributeIndex() {
    if (localVariableTableAttributeIndex == 0) {
      localVariableTableAttributeIndex = constantpool.newConstantUtf8("LocalVariableTable");
    }
  }

  /*
   * Creates a constant pool index for the InnerClasses attribute name.
   */
  private void createInnerClassesAttributeIndex() {
    if (innerClassesAttributeIndex == 0) {
      innerClassesAttributeIndex = constantpool.newConstantUtf8("InnerClasses");
      attributeCount++;
    }
  }

  /*
   * Creates a constant pool index for the SourceFile attribute name.
   */
  private void createSourceFileAttributeIndex() {
    if (sourceFileAttributeIndex == 0) {
      sourceFileAttributeIndex = constantpool.newConstantUtf8("SourceFile");
      attributeCount++;
    }
  }

  /*
   * Writes the binary data for the InnerClass attributes to the class data buffer.
   */
  private void generateInnerclassAttribute() {
    generate16(innerClassesAttributeIndex);
    generate32(2 + (innerclasses.size() * (2 + 2 + 2 + 2)));
    generate16((short) innerclasses.size());
    for (InnerClassAttribute innerclass : innerclasses) {
      innerclass.generate();
    }
  }

  /*
   * Writes the binary data for the SourceFile attribute to the class data buffer.
   */
  private void generateSourceFileAttribute() {
    generate16(sourceFileAttributeIndex);
    generate32(2);
    generate16(sourceFileIndex);
  }
}
