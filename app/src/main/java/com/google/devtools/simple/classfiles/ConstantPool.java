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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of the constant pool of a class file.
 * 
 * <p>For more information about the constant pool see the Java VM
 * specification.
 *
 * @author Herbert Czymontek
 */
final class ConstantPool {

  // Maximum constant pool index
  private static final int MAX_CONSTANTPOOL_INDEX = 0xFFFF;
  
  // Constant pool entry types
  private static final byte CONSTANT_Utf8 = 1;
  private static final byte CONSTANT_Integer = 3;
  private static final byte CONSTANT_Float = 4;
  private static final byte CONSTANT_Long = 5;
  private static final byte CONSTANT_Double = 6;
  private static final byte CONSTANT_Class = 7;
  private static final byte CONSTANT_String = 8;
  private static final byte CONSTANT_Fieldref = 9;
  private static final byte CONSTANT_Methodref = 10;
  private static final byte CONSTANT_InterfaceMethodref = 11;
  private static final byte CONSTANT_NameAndType = 12;

  // Next available constant pool index
  private int constantIndex;

  // List of constants in the constant pool
  private List<Constant> constantPool;

  // Mapping constant values to entries in the constant pool (this is used to eliminate
  // duplicate value in the constant pool)
  private Map<String, ConstantClass> classHashMap;
  private Map<StringTriple, ConstantFieldref> fieldrefHashMap;
  private Map<StringTriple, ConstantMethodref> methodrefHashMap;
  private Map<StringTriple, ConstantInterfaceMethodref> interfaceMethodrefHashMap;
  private Map<String, ConstantString> stringHashMap;
  private Map<Integer, ConstantInteger> integerHashMap;
  private Map<Float, ConstantFloat> floatHashMap;
  private Map<Long, ConstantLong> longHashMap;
  private Map<Double, ConstantDouble> doubleHashMap;
  private Map<StringPair, ConstantNameAndType> nameAndTypeHashMap;
  private Map<String, ConstantUtf8> utf8HashMap;

  /*
   * A pair of strings.
   */
  private static final class StringPair {
    private final String string1;
    private final String string2;

    /*
     * Creates a new pair strings.
     */
    StringPair(String string1, String string2) {
      this.string1 = Preconditions.checkNotNull(string1);
      this.string2 = Preconditions.checkNotNull(string2);
    }

    @Override
    public int hashCode() {
      return string1.hashCode() * 37 + string2.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      StringPair sp = (StringPair)obj;
      return string1.equals(sp.string1) && string2.equals(sp.string2);
    }
  }

  /*
   * A 3-tuple of string aka triple.
   */
  private static final class StringTriple {
    // Three strings defining the tuple
    private final String string1;
    private final String string2;
    private final String string3;

    /*
     * Creates a new string triple.
     */
    StringTriple(String string1, String string2, String string3) {
      this.string1 = Preconditions.checkNotNull(string1);
      this.string2 = Preconditions.checkNotNull(string2);
      this.string3 = Preconditions.checkNotNull(string3);
    }

    @Override
    public int hashCode() {
      return string1.hashCode() * 53 + string2.hashCode() * 37 + string3.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      StringTriple st = (StringTriple)obj;
      return string1.equals(st.string1) && string2.equals(st.string2) && string3.equals(st.string3);
    }
  }

  /*
   * Superclass for all constant types.
   */
  private abstract class Constant {
    // Constant type
    private final byte kind;

    // Constant pool index
    private final short index;

    /*
     * Creates a new Constant for the constant pool.
     */
    Constant(byte kind) {
      this.kind = kind;
      index = getNextConstantIndex();
    }

    /*
     * Returns the constant pool index of the Constant.
     */
    short getIndex() {
      return index;
    }

    /*
     * Generates the binary data for the Constant into class file.
     */
    void generate(ClassFile classFile) {
      classFile.generate8(kind);
    }
  }

  /*
   * There are several constant types that contain one  constant pool index. This is the superclass
   * for them.
   */
  private abstract class Constant1idx extends Constant {
    // Constant pool index #1
    private final short index1;

    Constant1idx(byte kind, short index1) {
      super(kind);
      this.index1 = index1;
    }

    @Override
    void generate(ClassFile classFile) {
      super.generate(classFile);
      classFile.generate16(index1);
    }
  }

  /*
   * There are several constant types that contain two constant pool index. This is the superclass
   * for them.
   */
  private abstract class Constant2idx extends Constant1idx {
    // Constant pool index #2
    private final short index2;

    Constant2idx(byte kind, short index1, short index2) {
      super(kind, index1);
      this.index2 = index2;
    }

    @Override
    void generate(ClassFile classFile) {
      super.generate(classFile);
      classFile.generate16(index2);
    }
  }

  /*
   * This is the superclass for Constants containing four bytes of data (like an int or a float).
   */
  private abstract class Constant4ub extends Constant {
    // Four bytes of data
    private final int fourBytes;

    Constant4ub(byte kind, int fourBytes) {
      super(kind);
      this.fourBytes = fourBytes;
    }

    @Override
    void generate(ClassFile classFile) {
      super.generate(classFile);
      classFile.generate32(fourBytes);
    }
  }

  /*
   * This is the superclass for Constants containing eight bytes of data (like a long or a double).
   */
  private abstract class Constant8ub extends Constant {
    // Eight bytes of data
    private long eightBytes;

    Constant8ub(byte kind, long eightBytes) {
      super(kind);
      this.eightBytes = eightBytes;
    }

    @Override
    void generate(ClassFile classFile) {
      super.generate(classFile);
      classFile.generate64(eightBytes);
    }
  }

  /*
   * This is the Constant for class references.
   */
  private final class ConstantClass extends Constant1idx {
    ConstantClass(short utf8Index) {
      super(CONSTANT_Class, utf8Index);
    }
  }

  /*
   * This is the Constant for field references.
   */
  private final class ConstantFieldref extends Constant2idx {
    ConstantFieldref(short classIndex, short nameAndTypeIndex) {
      super(CONSTANT_Fieldref, classIndex, nameAndTypeIndex);
    }
  }

  /*
   * This is the Constant for method references.
   */
  private final class ConstantMethodref extends Constant2idx {
    ConstantMethodref(short classIndex, short nameAndTypeIndex) {
      super(CONSTANT_Methodref, classIndex, nameAndTypeIndex);
    }
  }

  /*
   * This is the Constant for interface method references.
   */
  private final class ConstantInterfaceMethodref extends Constant2idx {
    ConstantInterfaceMethodref(short classIndex, short nameAndTypeIndex) {
      super(CONSTANT_InterfaceMethodref, classIndex, nameAndTypeIndex);
    }
  }

  /*
   * This is the Constant for string constants.
   */
  private final class ConstantString extends Constant1idx {
    ConstantString(short utf8Index) {
      super(CONSTANT_String, utf8Index);
    }
  }

  /*
   * This is the Constant for int constants.
   */
  private final class ConstantInteger extends Constant4ub {
    ConstantInteger(int value) {
      super(CONSTANT_Integer, value);
    }
  }

  /*
   * This is the Constant for float constants.
   */
  private final class ConstantFloat extends Constant4ub {
    ConstantFloat(float value) {
      super(CONSTANT_Float, Float.floatToRawIntBits(value));
    }
  }

  /*
   * This is the Constant for long constants.
   */
  private final class ConstantLong extends Constant8ub {
    ConstantLong(long value) {
      super(CONSTANT_Long, value);

      // Long constants require an additional index in the constant pool
      getNextConstantIndex();
    }
  }

  /*
   * This is the Constant for double constants.
   */
  private final class ConstantDouble extends Constant8ub {
    ConstantDouble(double value){
      super(CONSTANT_Double, Double.doubleToRawLongBits(value));

      // Double constants require an additional index in the constant pool
      getNextConstantIndex();
    }
  }

  /*
   * This is the Constant for name and type references.
   */
  private final class ConstantNameAndType extends Constant2idx {
    ConstantNameAndType(short nameIndex, short typeIndex){
      super(CONSTANT_NameAndType, nameIndex, typeIndex);
    }
  }

  /*
   * This is the Constant for string constant data (in UTF-8 encoding).
   */
  private final class ConstantUtf8 extends Constant {
    private byte[] string;

    ConstantUtf8(String string){
      super(CONSTANT_Utf8);
      this.string = string.getBytes(Charset.forName("UTF-8"));

      if (this.string.length > MAX_CONSTANTPOOL_INDEX) {
        throw new ConstantPoolOverflowException("utf8 constant too long");
      }
    }

    @Override
    void generate(ClassFile classFile) {
      super.generate(classFile);
      classFile.generate16((short)string.length);
      classFile.generateBytes(string);
    }
  }

  /**
   * Creates a new instance of ConstantPool.
   */
  protected ConstantPool() {
    constantIndex = 1;
    constantPool =  new ArrayList<Constant>();
    classHashMap = new HashMap<String, ConstantClass>();
    fieldrefHashMap = new HashMap<StringTriple, ConstantFieldref>();
    methodrefHashMap = new HashMap<StringTriple, ConstantMethodref>();
    interfaceMethodrefHashMap = new HashMap<StringTriple, ConstantInterfaceMethodref>();
    stringHashMap = new HashMap<String, ConstantString>();
    integerHashMap = new HashMap<Integer, ConstantInteger>();
    floatHashMap = new HashMap<Float, ConstantFloat>();
    longHashMap = new HashMap<Long, ConstantLong>();
    doubleHashMap = new HashMap<Double, ConstantDouble>();
    nameAndTypeHashMap = new HashMap<StringPair, ConstantNameAndType>();
    utf8HashMap = new HashMap<String, ConstantUtf8>();
  }

  /*
   * Return the next available constant pool index.
   */
  private short getNextConstantIndex() throws ConstantPoolOverflowException {
    if (constantIndex > MAX_CONSTANTPOOL_INDEX) {
      throw new ConstantPoolOverflowException("out of constant pool indices");
    }

    return (short)constantIndex++;
  }

  /**
   * Creates a new constant pool index for class references.
   * 
   * @param internalName  class name in internal format
   * @return  constant pool index
   */
  protected short newConstantClass(String internalName) {
    ConstantClass cc = classHashMap.get(internalName);
    if (cc == null) {
      cc = new ConstantClass(newConstantUtf8(internalName));
      classHashMap.put(internalName, cc);
      constantPool.add(cc);
    }

    return cc.getIndex();
  }

  /**
   * Creates a new constant pool index for field references.
   * 
   * @param className  class name in internal format
   * @param name  field name
   * @param signature  field signature
   * @return  constant pool index
   */
  protected short newConstantFieldref(String className, String name, String signature) {
    StringTriple st = new StringTriple(className, name, signature);
    ConstantFieldref cf = fieldrefHashMap.get(st);
    if (cf == null) {
      cf = new ConstantFieldref(newConstantClass(className),
          newConstantNameAndType(name, signature));
      fieldrefHashMap.put(st, cf);
      constantPool.add(cf);
    }

    return cf.getIndex();
  }

  /**
   * Creates a new constant pool index for method references.
   * 
   * @param className  class name in internal format
   * @param name  method name
   * @param signature  method signature
   * @return  constant pool index
   */
  protected short newConstantMethodref(String className, String name, String signature) {
    StringTriple st = new StringTriple(className, name, signature);
    ConstantMethodref cm = methodrefHashMap.get(st);
    if (cm == null) {
      cm = new ConstantMethodref(newConstantClass(className), 
          newConstantNameAndType(name, signature));
      methodrefHashMap.put(st, cm);
      constantPool.add(cm);
    }

    return cm.getIndex();
  }

  /**
   * Creates a new constant pool index for interface method references.
   * 
   * @param className  class name in internal format
   * @param name  method name
   * @param signature  method signature
   * @return  constant pool index
   */
  protected short newConstantInterfaceMethodref(String className, String name, String signature) {
    StringTriple st = new StringTriple(className, name, signature);
    ConstantInterfaceMethodref cim = interfaceMethodrefHashMap.get(st);
    if (cim == null) {
      cim = new ConstantInterfaceMethodref(newConstantClass(className), 
          newConstantNameAndType(name, signature));
      interfaceMethodrefHashMap.put(st, cim);
      constantPool.add(cim);
    }

    return cim.getIndex();
  }

  /**
   * Creates a new constant pool index for string constants.
   * 
   * @param string  string constant
   * @return  constant pool index
   */
  protected short newConstantString(String string) {
    ConstantString cs = stringHashMap.get(string);
    if (cs == null) {
      cs = new ConstantString(newConstantUtf8(string));
      stringHashMap.put(string, cs);
      constantPool.add(cs);
    }

    return cs.getIndex();
  }

  /**
   * Creates a new constant pool index for int constants.
   * 
   * @param i  int constant
   * @return  constant pool index
   */
  protected short newConstantInteger(int i) {
    Integer integer = Integer.valueOf(i);
    ConstantInteger ci = integerHashMap.get(integer);
    if (ci == null) {
      ci = new ConstantInteger(i);
      integerHashMap.put(integer, ci);
      constantPool.add(ci);
    }

    return ci.getIndex();
  }

  /**
   * Creates a new constant pool index for float constants.
   * 
   * @param f  float constant
   * @return  constant pool index
   */
  protected short newConstantFloat(float f) {
    Float flaot = Float.valueOf(f);
    ConstantFloat cf = floatHashMap.get(flaot);
    if (cf == null) {
      cf = new ConstantFloat(f);
      floatHashMap.put(flaot, cf);
      constantPool.add(cf);
    }

    return cf.getIndex();
  }

  /**
   * Creates a new constant pool index for long constants.
   * 
   * @param l  long constant
   * @return  constant pool index
   */
  protected short newConstantLong(long l) {
    Long lng = Long.valueOf(l);
    ConstantLong cl = longHashMap.get(lng);
    if (cl == null) {
      cl = new ConstantLong(l);
      longHashMap.put(lng, cl);
      constantPool.add(cl);
    }

    return cl.getIndex();
  }

  /**
   * Creates a new constant pool index for double constants.
   * 
   * @param d  double constant
   * @return  constant pool index
   */
  protected short newConstantDouble(double d) {
    Double duoble = Double.valueOf(d);
    ConstantDouble cd = doubleHashMap.get(duoble);
    if (cd == null) {
      cd = new ConstantDouble(d);
      doubleHashMap.put(duoble, cd);
      constantPool.add(cd);
    }

    return cd.getIndex();
  }

  /**
   * Creates a new constant pool index for name and type references.
   * 
   * @param name  name of reference
   * @param signature  type of reference
   * @return  constant pool index
   */
  protected short newConstantNameAndType(String name, String signature) {
    StringPair sp = new StringPair(name, signature);
    ConstantNameAndType cnt = nameAndTypeHashMap.get(sp);
    if (cnt == null) {
      cnt = new ConstantNameAndType(newConstantUtf8(name), newConstantUtf8(signature));
      nameAndTypeHashMap.put(sp, cnt);
      constantPool.add(cnt);
    }

    return cnt.getIndex();
  }

  /**
   * Creates a new constant pool index for string constant data encoded in UTF-8.
   * 
   * @param utf8String  string constant data encoded in UTF-8
   * @return  constant pool index
   */
  protected short newConstantUtf8(String utf8String) {
    ConstantUtf8 cu = utf8HashMap.get(utf8String);
    if (cu == null) {
      cu = new ConstantUtf8(utf8String);
      utf8HashMap.put(utf8String, cu);
      constantPool.add(cu);
    }

    return cu.getIndex();
  }

  /**
   * Generates binary data for the constants in the constant pool into class
   * file data buffer.
   * 
   * @param classFile  class file to generate into
   */
  protected void generate(ClassFile classFile) {
    classFile.generate16((short)constantIndex);
    for (Constant c : constantPool) {
      c.generate(classFile);
    }
  }
}
