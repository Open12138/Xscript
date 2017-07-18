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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a method in a class file.
 * 
 * <p>For more information about the method descriptors see the Java VM
 * specification.
 * 
 * <p>Note: Before you start generating code for a method, you must call
 * {@link #startCodeGeneration()}. After all code had been generated you must
 * not forget to call {@link #finishCodeGeneration()}.
 *
 * @author Herbert Czymontek
 */
public final class Method {

  /**
   * Defines a Label within the Code attribute of a method.
   */
  public static class Label {
    // Offset within the code that is being labeled
    private int offset;
  }

  /*
   * Defines a fixup that needs to be done after all labels have been resolved.
   */
  private static class Fixup {
    // Fixup kinds
    private static final byte FIXUP_BRANCH = 0; 

    // Offset in code buffer at which the fixup needs to be done
    private final int offset;

    // Kind of fixup to be done
    private final byte kind;

    // Label the fixup refers to
    private final Label label;

    /*
     * Creates a new fixup.
     */
    Fixup(byte kind, int offset, Label label) {
      this.kind = kind;
      this.offset = offset;
      this.label = label;
    }
  }

  /*
   * Defines an exception table entry for the method. For more information about exception tables
   * see the Java VM specification.
   */
  private class ExceptionTableEntry {
    // Analogous to the exception table entry definition of Java VM specification
    private final Label startLabel;
    private final Label endLabel;
    private final Label handlerLabel;
    private final short exceptionIndex;

    /*
     * Creates a new exception table entry.
     */
    ExceptionTableEntry(Label startLabel, Label endLabel, Label handlerLabel,
        short exceptionIndex) {
      this.startLabel = startLabel;
      this.endLabel = endLabel;
      this.handlerLabel = handlerLabel;
      this.exceptionIndex = exceptionIndex;
    }

    /*
     * Generates the exception table entry into the class file data buffer.
     */
    void generate() {
      classFile.generate16((short) startLabel.offset);
      classFile.generate16((short) endLabel.offset);
      classFile.generate16((short) handlerLabel.offset);
      classFile.generate16(exceptionIndex);
    }
  }

  /*
   * Defines a line number table entry. For more information about line number tables see the Java
   * VM specification.
   */
  private class LineNumberTableEntry {
    // Analogous to the line number table entry definition of Java VM specification
    private final short offset;
    private final short line;

    LineNumberTableEntry(short offset, short line) {
      this.offset = offset;
      this.line = line;
    }

    /*
     * Generates the line number table entry into the class file data buffer.
     */
    void generate() {
      classFile.generate16(offset);
      classFile.generate16(line);
    }
  }

  /*
   * Defines a local variable table entry. For more information about local variable tables see
   * the Java VM specification.
   */
  private class LocalVariableTableEntry {
    private final Label startLabel;
    private final Label endLabel;
    private final short name;
    private final short signature;
    private final short slot;

    LocalVariableTableEntry(Label startLabel, Label endLabel, short name, short signature,
        short slot) {
      this.startLabel = startLabel;
      this.endLabel = endLabel;
      this.name = name;
      this.signature = signature;
      this.slot = slot;
    }

    /*
     * Generates the local variable table entry into the class file data buffer.
     */
    void generate() {
      classFile.generate16((short) startLabel.offset);
      classFile.generate16((short) (endLabel.offset - startLabel.offset));
      classFile.generate16(name);
      classFile.generate16(signature);
      classFile.generate16(slot);
    }
  }

  // Maximum variable index
  private static final int MAX_VARIABLE_INDEX = 0xFFFF;

  /**
   * Method access flags.
   */
  public static final short ACC_PUBLIC = 0x0001;
  public static final short ACC_PRIVATE = 0x0002;
  public static final short ACC_PROTECTED = 0x0004;
  public static final short ACC_STATIC = 0x0008;
  public static final short ACC_FINAL = 0x0010;
  public static final short ACC_SYNCHRONIZED = 0x0020;
  public static final short ACC_BRIDGE = 0x0040;
  public static final short ACC_VARARGS = 0x0080;
  public static final short ACC_NATIVE = 0x01000;
  public static final short ACC_ABSTRACT = 0x0400;
  public static final short ACC_STRICT = 0x0800;
  public static final short ACC_SYNTHETIC = 0x1000;

  // Mask containing all valid method access flags
  private static final short ALLOWED_ACC_FLAGS = ACC_PUBLIC|ACC_PRIVATE |ACC_PROTECTED|ACC_STATIC|
      ACC_FINAL|ACC_SYNCHRONIZED|ACC_BRIDGE|ACC_VARARGS |ACC_NATIVE|ACC_ABSTRACT |ACC_STRICT|
      ACC_SYNTHETIC;

  // Constants for type designators (used by newarray instruction)
  private static final byte T_BOOLEAN = 4;
  private static final byte T_CHAR = 5;
  private static final byte T_FLOAT = 6;
  private static final byte T_DOUBLE = 7;
  private static final byte T_BYTE = 8;
  private static final byte T_SHORT = 9;
  private static final byte T_INT = 10;
  private static final byte T_LONG = 11;

  // Constants for bytecode mnemonics
  private static final byte INSTR_nop = (byte) 0x00;
  private static final byte INSTR_aconst_null = (byte) 0x01;
//private static final byte INSTR_iconst_m1 = (byte) 0x02;
  private static final byte INSTR_iconst_0 = (byte) 0x03;
//private static final byte INSTR_iconst_1 = (byte) 0x04;
//private static final byte INSTR_iconst_2 = (byte) 0x05;
//private static final byte INSTR_iconst_3 = (byte) 0x06;
//private static final byte INSTR_iconst_4 = (byte) 0x07;
//private static final byte INSTR_iconst_5 = (byte) 0x08;
  private static final byte INSTR_lconst_0 = (byte) 0x09;
  private static final byte INSTR_lconst_1 = (byte) 0x0A;
  private static final byte INSTR_fconst_0 = (byte) 0x0B;
  private static final byte INSTR_fconst_1 = (byte) 0x0C;
  private static final byte INSTR_fconst_2 = (byte) 0x0D;
  private static final byte INSTR_dconst_0 = (byte) 0x0E;
  private static final byte INSTR_dconst_1 = (byte) 0x0F;
  private static final byte INSTR_bipush = (byte) 0x10;
  private static final byte INSTR_sipush = (byte) 0x11;
  private static final byte INSTR_ldc = (byte) 0x12;
  private static final byte INSTR_ldc_w = (byte) 0x13;
  private static final byte INSTR_ldc2_w = (byte) 0x14;
  private static final byte INSTR_iload = (byte) 0x15;
  private static final byte INSTR_lload = (byte) 0x16;
  private static final byte INSTR_fload = (byte) 0x17;
  private static final byte INSTR_dload = (byte) 0x18;
  private static final byte INSTR_aload = (byte) 0x19;
  private static final byte INSTR_iload_0 = (byte) 0x1A;
//private static final byte INSTR_iload_1 = (byte) 0x1B;
//private static final byte INSTR_iload_2 = (byte) 0x1C;
//private static final byte INSTR_iload_3 = (byte) 0x1D;
  private static final byte INSTR_lload_0 = (byte) 0x1E;
//private static final byte INSTR_lload_1 = (byte) 0x1F;
//private static final byte INSTR_lload_2 = (byte) 0x20;
//private static final byte INSTR_lload_3 = (byte) 0x21;
  private static final byte INSTR_fload_0 = (byte) 0x22;
//private static final byte INSTR_fload_1 = (byte) 0x23;
//private static final byte INSTR_fload_2 = (byte) 0x24;
//private static final byte INSTR_fload_3 = (byte) 0x25;
  private static final byte INSTR_dload_0 = (byte) 0x26;
//private static final byte INSTR_dload_1 = (byte) 0x27;
//private static final byte INSTR_dload_2 = (byte) 0x28;
//private static final byte INSTR_dload_3 = (byte) 0x29;
  private static final byte INSTR_aload_0 = (byte) 0x2A;
//private static final byte INSTR_aload_1 = (byte) 0x2B;
//private static final byte INSTR_aload_2 = (byte) 0x2C;
//private static final byte INSTR_aload_3 = (byte) 0x2D;
  private static final byte INSTR_iaload = (byte) 0x2E;
  private static final byte INSTR_laload = (byte) 0x2F;
  private static final byte INSTR_faload = (byte) 0x30;
  private static final byte INSTR_daload = (byte) 0x31;
  private static final byte INSTR_aaload = (byte) 0x32;
  private static final byte INSTR_baload = (byte) 0x33;
  private static final byte INSTR_caload = (byte) 0x34;
  private static final byte INSTR_saload = (byte) 0x35;
  private static final byte INSTR_istore = (byte) 0x36;
  private static final byte INSTR_lstore = (byte) 0x37;
  private static final byte INSTR_fstore = (byte) 0x38;
  private static final byte INSTR_dstore = (byte) 0x39;
  private static final byte INSTR_astore = (byte) 0x3A;
  private static final byte INSTR_istore_0 = (byte) 0x3B;
//private static final byte INSTR_istore_1 = (byte) 0x3C;
//private static final byte INSTR_istore_2 = (byte) 0x3D;
//private static final byte INSTR_istore_3 = (byte) 0x3E;
  private static final byte INSTR_lstore_0 = (byte) 0x3F;
//private static final byte INSTR_lstore_1 = (byte) 0x40;
//private static final byte INSTR_lstore_2 = (byte) 0x41;
//private static final byte INSTR_lstore_3 = (byte) 0x42;
  private static final byte INSTR_fstore_0 = (byte) 0x43;
//private static final byte INSTR_fstore_1 = (byte) 0x44;
//private static final byte INSTR_fstore_2 = (byte) 0x45;
//private static final byte INSTR_fstore_3 = (byte) 0x46;
  private static final byte INSTR_dstore_0 = (byte) 0x47;
//private static final byte INSTR_dstore_1 = (byte) 0x48;
//private static final byte INSTR_dstore_2 = (byte) 0x49;
//private static final byte INSTR_dstore_3 = (byte) 0x4A;
  private static final byte INSTR_astore_0 = (byte) 0x4B;
//private static final byte INSTR_astore_1 = (byte) 0x4C;
//private static final byte INSTR_astore_2 = (byte) 0x4D;
//private static final byte INSTR_astore_3 = (byte) 0x4E;
  private static final byte INSTR_iastore = (byte) 0x4F;
  private static final byte INSTR_lastore = (byte) 0x50;
  private static final byte INSTR_fastore = (byte) 0x51;
  private static final byte INSTR_dastore = (byte) 0x52;
  private static final byte INSTR_aastore = (byte) 0x53;
  private static final byte INSTR_bastore = (byte) 0x54;
  private static final byte INSTR_castore = (byte) 0x55;
  private static final byte INSTR_sastore = (byte) 0x56;
  private static final byte INSTR_pop = (byte) 0x57;
  private static final byte INSTR_pop2 = (byte) 0x58;
  private static final byte INSTR_dup = (byte) 0x59;
  private static final byte INSTR_dup_x1 = (byte) 0x5A;
  private static final byte INSTR_dup_x2 = (byte) 0x5B;
  private static final byte INSTR_dup2 = (byte) 0x5C;
  private static final byte INSTR_dup2_x1 = (byte) 0x5D;
  private static final byte INSTR_dup2_x2 = (byte) 0x5E;
  private static final byte INSTR_swap = (byte) 0x5F;
  private static final byte INSTR_iadd = (byte) 0x60;
  private static final byte INSTR_ladd = (byte) 0x61;
  private static final byte INSTR_fadd = (byte) 0x62;
  private static final byte INSTR_dadd = (byte) 0x63;
  private static final byte INSTR_isub = (byte) 0x64;
  private static final byte INSTR_lsub = (byte) 0x65;
  private static final byte INSTR_fsub = (byte) 0x66;
  private static final byte INSTR_dsub = (byte) 0x67;
  private static final byte INSTR_imul = (byte) 0x68;
  private static final byte INSTR_lmul = (byte) 0x69;
  private static final byte INSTR_fmul = (byte) 0x6A;
  private static final byte INSTR_dmul = (byte) 0x6B;
  private static final byte INSTR_idiv = (byte) 0x6C;
  private static final byte INSTR_ldiv = (byte) 0x6D;
  private static final byte INSTR_fdiv = (byte) 0x6E;
  private static final byte INSTR_ddiv = (byte) 0x6F;
  private static final byte INSTR_irem = (byte) 0x70;
  private static final byte INSTR_lrem = (byte) 0x71;
  private static final byte INSTR_frem = (byte) 0x72;
  private static final byte INSTR_drem = (byte) 0x73;
  private static final byte INSTR_ineg = (byte) 0x74;
  private static final byte INSTR_lneg = (byte) 0x75;
  private static final byte INSTR_fneg = (byte) 0x76;
  private static final byte INSTR_dneg = (byte) 0x77;
  private static final byte INSTR_ishl = (byte) 0x78;
  private static final byte INSTR_lshl = (byte) 0x79;
  private static final byte INSTR_ishr = (byte) 0x7A;
  private static final byte INSTR_lshr = (byte) 0x7B;
  private static final byte INSTR_iushr = (byte) 0x7C;
  private static final byte INSTR_lushr = (byte) 0x7D;
  private static final byte INSTR_iand = (byte) 0x7E;
  private static final byte INSTR_land = (byte) 0x7F;
  private static final byte INSTR_ior = (byte) 0x80;
  private static final byte INSTR_lor = (byte) 0x81;
  private static final byte INSTR_ixor = (byte) 0x82;
  private static final byte INSTR_lxor = (byte) 0x83;
  private static final byte INSTR_iinc = (byte) 0x84;
  private static final byte INSTR_i2l = (byte) 0x85;
  private static final byte INSTR_i2f = (byte) 0x86;
  private static final byte INSTR_i2d = (byte) 0x87;
  private static final byte INSTR_l2i = (byte) 0x88;
  private static final byte INSTR_l2f = (byte) 0x89;
  private static final byte INSTR_l2d = (byte) 0x8A;
  private static final byte INSTR_f2i = (byte) 0x8B;
  private static final byte INSTR_f2l = (byte) 0x8C;
  private static final byte INSTR_f2d = (byte) 0x8D;
  private static final byte INSTR_d2i = (byte) 0x8E;
  private static final byte INSTR_d2l = (byte) 0x8F;
  private static final byte INSTR_d2f = (byte) 0x90;
  private static final byte INSTR_i2b = (byte) 0x91;
  private static final byte INSTR_i2c = (byte) 0x92;
  private static final byte INSTR_i2s = (byte) 0x93;
  private static final byte INSTR_lcmp = (byte) 0x94;
  private static final byte INSTR_fcmpl = (byte) 0x95;
  private static final byte INSTR_fcmpg = (byte) 0x96;
  private static final byte INSTR_dcmpl = (byte) 0x97;
  private static final byte INSTR_dcmpg = (byte) 0x98;
  private static final byte INSTR_ifeq = (byte) 0x99;
  private static final byte INSTR_ifne = (byte) 0x9A;
  private static final byte INSTR_iflt = (byte) 0x9B;
  private static final byte INSTR_ifge = (byte) 0x9C;
  private static final byte INSTR_ifgt = (byte) 0x9D;
  private static final byte INSTR_ifle = (byte) 0x9E;
  private static final byte INSTR_if_icmpeq = (byte) 0x9F;
  private static final byte INSTR_if_icmpne = (byte) 0xA0;
  private static final byte INSTR_if_icmplt = (byte) 0xA1;
  private static final byte INSTR_if_icmpge = (byte) 0xA2;
  private static final byte INSTR_if_icmpgt = (byte) 0xA3;
  private static final byte INSTR_if_icmple = (byte) 0xA4;
  private static final byte INSTR_if_acmpeq = (byte) 0xA5;
  private static final byte INSTR_if_acmpne = (byte) 0xA6;
  private static final byte INSTR_goto = (byte) 0xA7;
  private static final byte INSTR_jsr = (byte) 0xA8;
  private static final byte INSTR_ret = (byte) 0xA9;
//private static final byte INSTR_tableswitch = (byte) 0xAA;
//private static final byte INSTR_lookupswitch = (byte) 0xAB;
  private static final byte INSTR_ireturn = (byte) 0xAC;
  private static final byte INSTR_lreturn = (byte) 0xAD;
  private static final byte INSTR_freturn = (byte) 0xAE;
  private static final byte INSTR_dreturn = (byte) 0xAF;
  private static final byte INSTR_areturn = (byte) 0xB0;
  private static final byte INSTR_return = (byte) 0xB1;
  private static final byte INSTR_getstatic = (byte) 0xB2;
  private static final byte INSTR_putstatic = (byte) 0xB3;
  private static final byte INSTR_getfield = (byte) 0xB4;
  private static final byte INSTR_putfield = (byte) 0xB5;
  private static final byte INSTR_invokevirtual = (byte) 0xB6;
  private static final byte INSTR_invokespecial = (byte) 0xB7;
  private static final byte INSTR_invokestatic = (byte) 0xB8;
  private static final byte INSTR_invokeinterface = (byte) 0xB9;
  private static final byte INSTR_new = (byte) 0xBB;
  private static final byte INSTR_newarray = (byte) 0xBC;
  private static final byte INSTR_anewarray = (byte) 0xBD;
  private static final byte INSTR_arraylength = (byte) 0xBE;
  private static final byte INSTR_athrow = (byte) 0xBF;
  private static final byte INSTR_checkcast = (byte) 0xC0;
  private static final byte INSTR_instanceof = (byte) 0xC1;
  private static final byte INSTR_monitorenter = (byte) 0xC2;
  private static final byte INSTR_monitorexit = (byte) 0xC3;
  private static final byte INSTR_wide = (byte) 0xC4;
  private static final byte INSTR_multianewarray = (byte) 0xC5;
  private static final byte INSTR_ifnull = (byte) 0xC6;
  private static final byte INSTR_ifnonnull = (byte) 0xC7;
//private static final byte INSTR_goto_w = (byte) 0xC8;
//private static final byte INSTR_jsr_w = (byte) 0xC9;

  // Class file defining this method
  private final ClassFile classFile;

  // Constant pool indices of method name and signature
  private final short nameIndex;
  private final short signatureIndex;

  // Access flags for method
  private final short flags;

  // Number of local variable slots required by the method
  private int localsCount;

  // Maximum number of operand stack slots required by the method
  private int maxOpStackLevel;

  // Used for calculating the maximum number of operand stack slots required; updated after
  // generation of each instruction
  private int opStackLevel;

  // Method attribute count
  private short attributeCount;

  // Constant pool index of Code attribute name (0 if not used)
  private short codeAttributeIndex;

  // List of exception table entries for the method
  private List<ExceptionTableEntry> exceptionsAttributes;

  // List of line number table entries for the method
  private List<LineNumberTableEntry> lineNumbers;

  // List of local variable table entries for the method
  private List<LocalVariableTableEntry> localVariables;

  // Constant pool index of LineNumberTable attribute name (0 if not used)
  private short lineNumberTableAttributeIndex;

  // Constant pool index of LocalVariableTable attribute name (initialized lazily, indicating that
  // method does have a LocalVariableTable attribute)
  private short localVariableTableAttributeIndex;

  // Runtime visible annotations
  private AnnotationsAttribute runtimeVisibleAnnotationsAttribute;

  // Buffer holding code during generation
  private Buffer buffer;

  // List with fixups in the generated code
  private List<Fixup> fixups;

  // Array containing generated code after byte code generation completed
  private byte[] bytecode;

  /**
   * Defines a new method for a class file.
   * 
   * @param classFile  class file containing the method
   * @param flags  method access flags
   * @param name  method name
   * @param signature  method signature
   */
  public Method(ClassFile classFile, short flags, String name, String signature) {
    Preconditions.checkArgument((flags & ALLOWED_ACC_FLAGS) == flags);

    this.flags = flags;
    this.classFile = classFile;
    nameIndex = classFile.constantpool.newConstantUtf8(name);
    signatureIndex = classFile.constantpool.newConstantUtf8(signature);

    setLocals(getSlotCountFromSignature(signature, (flags & ACC_STATIC) == 0));
  }

  /*
   * Updates the number of local variable slots.
   */
  private void setLocals(int maxLocal) {
    if (maxLocal > MAX_VARIABLE_INDEX) {
      throw new MethodTooBigException("too many local variables");
    }

    if (maxLocal > localsCount)
      localsCount = maxLocal;
  }

  /*
   * Returns the number of variable or operand slots required by a type. This assumes signature
   * to be a separate type.
   */
  private int getTypeSize(String signature) {
    return getTypeSize(signature, 0);
  }

  /*
   * Returns the number of variable or operand slots required by the type within a signature.
   */
  private int getTypeSize(String signature, int index) {
    switch (signature.charAt(index)) {
      default:
        return 1;

      case 'V':
        return 0;

      case 'J':
      case 'D':
        return 2;
    }
  }

  /*
   * Computes the number of arguments from a method signature.
   */
  private int getArgumentCountFromSignature(String signature, boolean hasThis) {
    return getCountFromSignature(signature, hasThis, false);
  }

  /*
   * Returns the number of local variable slots required by method signature.
   */
  private int getSlotCountFromSignature(String signature, boolean hasThis) {
    return getCountFromSignature(signature, hasThis, true);
  }

  /*
   * Counts arguments (or slots) based on a method signature.
   */ 
  @SuppressWarnings("fallthrough")
  private int getCountFromSignature(String signature, boolean hasThis, boolean countSlots) {
    char[] sig = signature.toCharArray();
    int count = hasThis ? 1 : 0;

    Preconditions.checkArgument(sig[0] == '(');
    int index = 1;

    for (;;) {
      switch (sig[index++]) {
        default:
          // COV_NF_START
          Compiler.internalError();
          break;
          // COV_NF_END

        case 'J':
        case 'D':
          if (countSlots) {
            // long and double need an extra slot!
            count++;
          }
          // Fall thru...

        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I':
        case 'F':
          break;

        case '[':
          while (sig[index] == '[') {
            index++;
          }
          if (sig[index++] != 'L') {
            break;
          }
          // Fall thru...

        case 'L':
          while (sig[index++] != ';') {
          }
          break;

        case ')':
          // End of parameter type list in signature reached, return type irrelevant
          return count;
      }

      // Another slot needed
      count++;
    }
  }

  /*
   * Calculates the effect of an invoke instruction on the operand stack. Returns the
   * change in number of operand stack slots.
   */
  @SuppressWarnings("fallthrough")
  private int getInvokeStackEffect(String signature, boolean hasThis) {
    char[] sig = signature.toCharArray();
    int count = hasThis ? -1 : 0;

    Preconditions.checkArgument(sig[0] == '(');
    int index = 1;

    for (;;) {
      switch (sig[index++]) {
        default:
          // COV_NF_START
          Compiler.internalError();
          break;
          // COV_NF_END

        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I':
        case 'F':
          break;

        case 'J':
        case 'D':
          // Long and double occupy two operand stack slots
          count--;
          break;

        case '[':
          while (sig[index] == '[') {
            index++;
          }
          if (sig[index++] != 'L') {
            break;
          }
          // Fall thru...

        case 'L':
          while (sig[index++] != ';') {
          }
          break;

        case ')':
          return count + getTypeSize(signature, index);
      }

      count--;
    }
  }

  /*
   * Adjusts the current operand stack level by the indicated amount, adjusting the maximum
   * operand stack slots required if necessary.
   */
  private void adjustOpstackLevel(int adjustment) {
    opStackLevel += adjustment;
    if (opStackLevel > maxOpStackLevel) {
      maxOpStackLevel = opStackLevel;
      if (maxOpStackLevel > MAX_VARIABLE_INDEX) {
        throw new MethodTooBigException("operand stack overflow");
      }
    }
  }

  /*
   * Generates an instruction and adjusts the operand stack level.
   */
  private void generateInstr(byte instruction, int oslAdjustment) {
    buffer.generate8(instruction);
    adjustOpstackLevel(oslAdjustment);
  }

  /*
   * Generates an instruction plus an additional byte and adjusts the operand stack level.
   */
  private void generateInstrByte(byte instruction, byte b, int oslAdjustment) {
    buffer.generate8_8(instruction, b);
    adjustOpstackLevel(oslAdjustment);
  }

  /*
   * Generates an instruction plus an additional short and adjusts the operand stack level.
   */
  private void generateInstrShort(byte instruction, short s, int oslAdjustment) {
    buffer.generate8_16(instruction, s);
    adjustOpstackLevel(oslAdjustment);
  }

  /*
   * Generates an instruction plus an additional short and a byte and adjusts the operand stack
   * level.
   */
  private void generateInstrShortByte(byte instruction, short s, byte b, int oslAdjustment) {
    buffer.generate8_16_8(instruction, s, b);
    adjustOpstackLevel(oslAdjustment);
  }

  /*
   * Generates an instruction plus an additional short and two bytes and adjusts the operand stack
   * level.
   */
  private void generateInstrShortByteByte(byte instruction, short s, byte b1, byte b2,
      int oslAdjustment) {
    buffer.generate8_16_8_8(instruction, s, b1, b2);
    adjustOpstackLevel(oslAdjustment);
  }

  /*
   * Generates a branch instruction and adjusts the operand stack level.
   */
  private void generateInstrBranch(byte instruction, Label label, int oslAdjustment) {
    fixups.add(new Fixup(Fixup.FIXUP_BRANCH, buffer.getOffset(), label));
    buffer.generate8_16(instruction, (short)0);
    adjustOpstackLevel(oslAdjustment);
  }

  /*
   * Generates an instruction to access a local variable and adjusts the operand stack level.
   */
  private void generateLocalAccessInstr(byte instruction, byte shortInstruction, short varnum,
      int oslAdjustment) {
    switch (varnum) {
      default:
        if (0x00 <= varnum && varnum <= 0xFF) {
          generateInstrByte(instruction, (byte) varnum, oslAdjustment);
        } else {
          // Accessors for local variables outside of the byte range require an instruction prefix
          generateInstr(INSTR_wide, 0);
          generateInstrShort(instruction, varnum, oslAdjustment);
        }
        break;

      case 0:
      case 1:
      case 2:
      case 3:
        // Locals 0 through 3 can use the compact form
        generateInstr((byte) (shortInstruction + varnum), oslAdjustment);
        break;
    }

    setLocals(varnum + Math.abs(oslAdjustment));
  }

  /**
   * Generates an aaload instruction.
   */
  public void generateInstrAaload() {
    generateInstr(INSTR_aaload, -1);
  }

  /**
   * Generates an aastore instruction.
   */
  public void generateInstrAastore() {
    generateInstr(INSTR_aastore, -3);
  }

  /**
   * Generates an aconst_null instruction.
   */
  public void generateInstrAconstNull() {
    generateInstr(INSTR_aconst_null, +1);
  }

  /**
   * Generates an aload instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load
   */
  public void generateInstrAload(short varnum) {
    generateLocalAccessInstr(INSTR_aload, INSTR_aload_0, varnum, +1);
  }

  /**
   * Generates an anewarray instruction.
   * 
   * @param internalName  signature of array type
   */
  public void generateInstrAnewarray(String internalName) {
    generateInstrShort(INSTR_anewarray, classFile.constantpool.newConstantClass(internalName), 0);
  }

  /**
   * Generates an areturn instruction.
   */
  public void generateInstrAreturn() {
    generateInstr(INSTR_areturn, -1);
  }

  /**
   * Generates an arraylength instruction.
   */
  public void generateInstrArraylength() {
    generateInstr(INSTR_arraylength, 0);
  }

  /**
   * Generates an astore instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to store
   */
  public void generateInstrAstore(short varnum) {
    generateLocalAccessInstr(INSTR_astore, INSTR_astore_0, varnum, -1);
  }

  /**
   * Generates an athrow instruction.
   */
  public void generateInstrAthrow() {
    generateInstr(INSTR_athrow, -1);
  }

  /**
   * Generates a baload instruction.
   */
  public void generateInstrBaload() {
    generateInstr(INSTR_baload, -1);
  }

  /**
   * Generates a bastore instruction.
   */
  public void generateInstrBastore() {
    generateInstr(INSTR_bastore, -3);
  }

  /**
   * Generates a caload instruction.
   */
  public void generateInstrCaload() {
    generateInstr(INSTR_caload, -1);
  }

  /**
   * Generates a castore instruction.
   */
  public void generateInstrCastore() {
    generateInstr(INSTR_castore, -3);
  }

  /**
   * Generates a checkcast instruction.
   * 
   * @param internalName  signature of type to check
   */
  public void generateInstrCheckcast(String internalName) {
    generateInstrShort(INSTR_checkcast, classFile.constantpool.newConstantClass(internalName), 0);
  }

  /**
   * Generates a d2f instruction.
   */
  public void generateInstrD2f() {
    generateInstr(INSTR_d2f, -1);
  }

  /**
   * Generates a d2i instruction.
   */
  public void generateInstrD2i() {
    generateInstr(INSTR_d2i, -1);
  }

  /**
   * Generates a d2l instruction.
   */
  public void generateInstrD2l() {
    generateInstr(INSTR_d2l, 0);
  }

  /**
   * Generates a dadd instruction.
   */
  public void generateInstrDadd() {
    generateInstr(INSTR_dadd, -2);
  }

  /**
   * Generates a daload instruction.
   */
  public void generateInstrDaload() {
    generateInstr(INSTR_daload, 0);
  }

  /**
   * Generates a dastore instruction.
   */
  public void generateInstrDastore() {
    generateInstr(INSTR_dastore, -4);
  }

  /**
   * Generates a dcmpg instruction.
   */
  public void generateInstrDcmpg() {
    generateInstr(INSTR_dcmpg, -3);
  }

  /**
   * Generates a dcmpl instruction.
   */
  public void generateInstrDcmpl() {
    generateInstr(INSTR_dcmpl, -3);
  }

  /**
   * Generates a ddiv instruction.
   */
  public void generateInstrDdiv() {
    generateInstr(INSTR_ddiv, -2);
  }

  /**
   * Generates a dload instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load
   */
  public void generateInstrDload(short varnum) {
    generateLocalAccessInstr(INSTR_dload, INSTR_dload_0, varnum, +2);
  }

  /**
   * Generates a dmul instruction.
   */
  public void generateInstrDmul() {
    generateInstr(INSTR_dmul, -2);
  }

  /**
   * Generates a dneg instruction.
   */
  public void generateInstrDneg() {
    generateInstr(INSTR_dneg, 0);
  }

  /**
   * Generates a drem instruction.
   */
  public void generateInstrDrem() {
    generateInstr(INSTR_drem, -2);
  }

  /**
   * Generates a dreturn instruction.
   */
  public void generateInstrDreturn() {
    generateInstr(INSTR_dreturn, -2);
  }

  /**
   * Generates a dstore instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to store
   */
  public void generateInstrDstore(short varnum) {
    generateLocalAccessInstr(INSTR_dstore, INSTR_dstore_0, varnum, -2);
  }

  /**
   * Generates a dsub instruction.
   */
  public void generateInstrDsub() {
    generateInstr(INSTR_dsub, -2);
  }

  /**
   * Generates a dup instruction.
   */
  public void generateInstrDup() {
    generateInstr(INSTR_dup, +1);
  }

  /**
   * Generates a dup_x1 instruction.
   */
  public void generateInstrDupX1() {
    generateInstr(INSTR_dup_x1, +1);
  }

  /**
   * Generates a dup_x2 instruction.
   */
  public void generateInstrDupX2() {
    generateInstr(INSTR_dup_x2, +1);
  }

  /**
   * Generates a dup2 instruction.
   */
  public void generateInstrDup2() {
    generateInstr(INSTR_dup2, +2);
  }

  /**
   * Generates a dup2_x1 instruction.
   */
  public void generateInstrDup2X1() {
    generateInstr(INSTR_dup2_x1, +2);
  }

  /**
   * Generates a dup2_x2 instruction.
   */
  public void generateInstrDup2X2() {
    generateInstr(INSTR_dup2_x2, +2);
  }

  /**
   * Generates an f2d instruction.
   */
  public void generateInstrF2d() {
    generateInstr(INSTR_f2d, +1);
  }

  /**
   * Generates an f2i instruction.
   */
  public void generateInstrF2i() {
    generateInstr(INSTR_f2i, 0);
  }

  /**
   * Generates an f2l instruction.
   */
  public void generateInstrF2l() {
    generateInstr(INSTR_f2l, +1);
  }

  /**
   * Generates an fadd instruction.
   */
  public void generateInstrFadd() {
    generateInstr(INSTR_fadd, -1);
  }

  /**
   * Generates an faload instruction.
   */
  public void generateInstrFaload() {
    generateInstr(INSTR_faload, -1);
  }

  /**
   * Generates an fastore instruction.
   */
  public void generateInstrFastore() {
    generateInstr(INSTR_fastore, -3);
  }

  /**
   * Generates an fcmpg instruction.
   */
  public void generateInstrFcmpg() {
    generateInstr(INSTR_fcmpg, -1);
  }

  /**
   * Generates an fcmpl instruction.
   */
  public void generateInstrFcmpl() {
    generateInstr(INSTR_fcmpl, -1);
  }

  /**
   * Generates an fdiv instruction.
   */
  public void generateInstrFdiv() {
    generateInstr(INSTR_fdiv, -1);
  }

  /**
   * Generates an fload instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load
   */
  public void generateInstrFload(short varnum) {
    generateLocalAccessInstr(INSTR_fload, INSTR_fload_0, varnum, +1);
  }

  /**
   * Generates an fmul instruction.
   */
  public void generateInstrFmul() {
    generateInstr(INSTR_fmul, -1);
  }

  /**
   * Generates an fneg instruction.
   */
  public void generateInstrFneg() {
    generateInstr(INSTR_fneg, 0);
  }

  /**
   * Generates an frem instruction.
   */
  public void generateInstrFrem() {
    generateInstr(INSTR_frem, -1);
  }

  /**
   * Generates an freturn instruction.
   */
  public void generateInstrFreturn() {
    generateInstr(INSTR_freturn, -1);
  }

  /**
   * Generates an fstore instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to store
   */
  public void generateInstrFstore(short varnum) {
    generateLocalAccessInstr(INSTR_fstore, INSTR_fstore_0, varnum, -1);
  }

  /**
   * Generates an fsub instruction.
   */
  public void generateInstrFsub() {
    generateInstr(INSTR_fsub, -1);
  }

  /**
   * Generates a getfield instruction.
   * 
   * @param className  class name of field in internal format
   * @param name  field name
   * @param signature  field signature
   */
  public void generateInstrGetfield(String className, String name, String signature) {
    generateInstrShort(INSTR_getfield,
        classFile.constantpool.newConstantFieldref(className, name, signature),
        (short) (+getTypeSize(signature) - 1));
  }

  /**
   * Generates a getstatic instruction.
   * 
   * @param className  class name of field in internal format
   * @param name  field name
   * @param signature  field signature
   */
  public void generateInstrGetstatic(String className, String name, String signature) {
    generateInstrShort(INSTR_getstatic,
        classFile.constantpool.newConstantFieldref(className, name, signature),
        (short) +getTypeSize(signature));
  }

  /**
   * Generates a goto instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrGoto(Label label) {
    generateInstrBranch(INSTR_goto, label, 0);
  }

  /**
   * Generates an i2b instruction.
   */
  public void generateInstrI2b() {
    generateInstr(INSTR_i2b, 0);
  }

  /**
   * Generates an i2c instruction.
   */
  public void generateInstrI2c() {
    generateInstr(INSTR_i2c, 0);
  }

  /**
   * Generates an i2d instruction.
   */
  public void generateInstrI2d() {
    generateInstr(INSTR_i2d, +1);
  }

  /**
   * Generates an i2f instruction.
   */
  public void generateInstrI2f() {
    generateInstr(INSTR_i2f, 0);
  }

  /**
   * Generates an i2l instruction.
   */
  public void generateInstrI2l() {
    generateInstr(INSTR_i2l, +1);
  }

  /**
   * Generates an i2s instruction.
   */
  public void generateInstrI2s() {
    generateInstr(INSTR_i2s, 0);
  }

  /**
   * Generates an iadd instruction.
   */
  public void generateInstrIadd() {
    generateInstr(INSTR_iadd, -1);
  }

  /**
   * Generates an iaload instruction.
   */
  public void generateInstrIaload() {
    generateInstr(INSTR_iaload, -1);
  }

  /**
   * Generates an iand instruction.
   */
  public void generateInstrIand() {
    generateInstr(INSTR_iand, -1);
  }

  /**
   * Generates an iastore instruction.
   */
  public void generateInstrIastore() {
    generateInstr(INSTR_iastore, -3);
  }

  /**
   * Generates an idiv instruction.
   */
  public void generateInstrIdiv() {
    generateInstr(INSTR_idiv, -1);
  }

  /**
   * Generates an if_acmpeq instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfAcmpeq(Label label) {
    generateInstrBranch(INSTR_if_acmpeq, label, -2);
  }

  /**
   * Generates an if_acmpne instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfAcmpne(Label label) {
    generateInstrBranch(INSTR_if_acmpne, label, -2);
  }

  /**
   * Generates an if_icmpeq instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfIcmpeq(Label label) {
    generateInstrBranch(INSTR_if_icmpeq, label, -2);
  }

  /**
   * Generates an if_icmpne instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfIcmpne(Label label) {
    generateInstrBranch(INSTR_if_icmpne, label, -2);
  }

  /**
   * Generates an if_icmpge instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfIcmpge(Label label) {
    generateInstrBranch(INSTR_if_icmpge, label, -2);
  }

  /**
   * Generates an if_icmpgt instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfIcmpgt(Label label) {
    generateInstrBranch(INSTR_if_icmpgt, label, -2);
  }

  /**
   * Generates an if_icmple instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfIcmple(Label label) {
    generateInstrBranch(INSTR_if_icmple, label, -2);
  }

  /**
   * Generates an if_icmplt instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfIcmplt(Label label) {
    generateInstrBranch(INSTR_if_icmplt, label, -2);
  }

  /**
   * Generates an ifeq instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfeq(Label label) {
    generateInstrBranch(INSTR_ifeq, label, -1);
  }

  /**
   * Generates an ifne instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfne(Label label) {
    generateInstrBranch(INSTR_ifne, label, -1);
  }

  /**
   * Generates an iflt instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIflt(Label label) {
    generateInstrBranch(INSTR_iflt, label, -1);
  }

  /**
   * Generates an ifge instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfge(Label label) {
    generateInstrBranch(INSTR_ifge, label, -1);
  }

  /**
   * Generates an ifgt instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfgt(Label label) {
    generateInstrBranch(INSTR_ifgt, label, -1);
  }

  /**
   * Generates an ifle instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfle(Label label) {
    generateInstrBranch(INSTR_ifle, label, -1);
  }

  /**
   * Generates an ifnonnull instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfnonnull(Label label) {
    generateInstrBranch(INSTR_ifnonnull, label, -1);
  }

  /**
   * Generates an ifnull instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrIfnull(Label label) {
    generateInstrBranch(INSTR_ifnull, label, -1);
  }

  /**
   * Generates an iinc instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load
   * @param value  amount by which to increase (decrease) the local variable 
   */
  public void generateInstrIinc(short varnum, byte value) {
    if (0x00 <= varnum && varnum <= 0xFF) {
      buffer.generate8_8_8(INSTR_iinc, (byte) varnum, value);
    } else {
      buffer.generate8_8_16_8(INSTR_wide, INSTR_iinc, varnum, value);
    }
    // opStackEffect: 0
  }

  /**
   * Generates an iload instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load
   */
  public void generateInstrIload(short varnum) {
    generateLocalAccessInstr(INSTR_iload, INSTR_iload_0, varnum, +1);
  }

  /**
   * Generates an imul instruction.
   */
  public void generateInstrImul() {
    generateInstr(INSTR_imul, -1);
  }

  /**
   * Generates an ineg instruction.
   */
  public void generateInstrIneg() {
    generateInstr(INSTR_ineg, 0);
  }

  /**
   * Generates an instanceof instruction.
   * 
   * @param internalName  signature of type to check
   */
  public void generateInstrInstanceof(String internalName) {
    generateInstrShort(INSTR_instanceof, classFile.constantpool.newConstantClass(internalName), 0);
  }

  /**
   * Generates an invokeinterface instruction.
   * 
   * @param className  class name of method in internal format
   * @param name  method name
   * @param signature  method signature
   */
  public void generateInstrInvokeinterface(String className, String name, String signature) {
    generateInstrShortByteByte(INSTR_invokeinterface, 
        classFile.constantpool.newConstantInterfaceMethodref(className, name, signature),
        (byte) getArgumentCountFromSignature(signature, true), (byte) 0,
        getInvokeStackEffect(signature, true));
  }

  /**
   * Generates an invokespecial instruction.
   * 
   * @param className  class name of method in internal format
   * @param name  method name
   * @param signature  method signature
   */
  public void generateInstrInvokespecial(String className, String name, String signature) {
    generateInstrShort(INSTR_invokespecial, 
        classFile.constantpool.newConstantMethodref(className, name, signature),
        getInvokeStackEffect(signature, true));
  }

  /**
   * Generates an invokestatic instruction.
   * 
   * @param className  class name of method in internal format
   * @param name  method name
   * @param signature  method signature
   */
  public void generateInstrInvokestatic(String className, String name, String signature) {
    generateInstrShort(INSTR_invokestatic, 
        classFile.constantpool.newConstantMethodref(className, name, signature),
        getInvokeStackEffect(signature, false));
  }

  /**
   * Generates an invokevirtual instruction.
   * 
   * @param className  class name of method in internal format
   * @param name  method name
   * @param signature  method signature
   */
  public void generateInstrInvokevirtual(String className, String name, String signature) {
    generateInstrShort(INSTR_invokevirtual, 
        classFile.constantpool.newConstantMethodref(className, name, signature),
        getInvokeStackEffect(signature, true));
  }

  /**
   * Generates an ior instruction.
   */
  public void generateInstrIor() {
    generateInstr(INSTR_ior, -1);
  }

  /**
   * Generates an irem instruction.
   */
  public void generateInstrIrem() {
    generateInstr(INSTR_irem, -1);
  }

  /**
   * Generates an ireturn instruction.
   */
  public void generateInstrIreturn() {
    generateInstr(INSTR_ireturn, -1);
  }

  /**
   * Generates an ishl instruction.
   */
  public void generateInstrIshl() {
    generateInstr(INSTR_ishl, -1);
  }

  /**
   * Generates an ishr instruction.
   */
  public void generateInstrIshr() {
    generateInstr(INSTR_ishr, -1);
  }

  /**
   * Generates an istore instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to store
   */
  public void generateInstrIstore(short varnum) {
    generateLocalAccessInstr(INSTR_istore, INSTR_istore_0, varnum, -1);
  }

  /**
   * Generates an isub instruction.
   */
  public void generateInstrIsub() {
    generateInstr(INSTR_isub, -1);
  }

  /**
   * Generates an iushr instruction.
   */
  public void generateInstrIushr() {
    generateInstr(INSTR_iushr, -1);
  }

  /**
   * Generates an ixor instruction.
   */
  public void generateInstrIxor() {
    generateInstr(INSTR_ixor, -1);
  }

  /**
   * Generates a jsr instruction.
   * 
   * @param label  label to branch to
   */
  public void generateInstrJsr(Label label) {
    generateInstrBranch(INSTR_jsr, label, 0);
  }

  /**
   * Generates an l2d instruction.
   */
  public void generateInstrL2d() {
    generateInstr(INSTR_l2d, 0);
  }

  /**
   * Generates an l2f instruction.
   */
  public void generateInstrL2f() {
    generateInstr(INSTR_l2f, -1);
  }

  /**
   * Generates an l2i instruction.
   */
  public void generateInstrL2i() {
    generateInstr(INSTR_l2i, -1);
  }

  /**
   * Generates an ladd instruction.
   */
  public void generateInstrLadd() {
    generateInstr(INSTR_ladd, -2);
  }

  /**
   * Generates an laload instruction.
   */
  public void generateInstrLaload() {
    generateInstr(INSTR_laload, 0);
  }

  /**
   * Generates an land instruction.
   */
  public void generateInstrLand() {
    generateInstr(INSTR_land, -2);
  }

  /**
   * Generates an lastore instruction.
   */
  public void generateInstrLastore() {
    generateInstr(INSTR_lastore, -4);
  }

  /**
   * Generates an lcmp instruction.
   */
  public void generateInstrLcmp() {
    generateInstr(INSTR_lcmp, -3);
  }

  /**
   * Generates an ldc instruction (or an optimized form of it).
   * 
   * @param value  integer constant to load
   */
  public void generateInstrLdc(int value) {
    switch (value) {
      default:
        if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
          generateInstrByte(INSTR_bipush, (byte) value, +1);
        } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
          generateInstrShort(INSTR_sipush, (short) value, +1);
        } else {
          short index = classFile.constantpool.newConstantInteger(value);
          if (0x00 <= index && index <= 0xFF) {
            generateInstrByte(INSTR_ldc, (byte) index, +1);
          } else {
            generateInstrShort(INSTR_ldc_w, index, +1);
          }
        }
        break;

      case -1:
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
        generateInstr((byte)(INSTR_iconst_0 + value), +1);
        break;
    }
  }

  /**
   * Generates an ldc instruction (or an optimized form of it).
   * 
   * @param value  float constant to load
   */
  public void generateInstrLdc(float value) {
    if (value == 0f) {
      generateInstr(INSTR_fconst_0, +1);
    } else if (value == 1f) {
      generateInstr(INSTR_fconst_1, +1);
    } else if (value == 2f) {
      generateInstr(INSTR_fconst_2, +1);
    } else {
      short index = classFile.constantpool.newConstantFloat(value);
      if (0x00 <= index && index <= 0xFF) {
        generateInstrByte(INSTR_ldc, (byte) index, +1);
      } else {
        generateInstrShort(INSTR_ldc_w, index, +1);
      }
    }
  }

  /**
   * Generates an ldc instruction (or an optimized form of it).
   * 
   * @param value  string constant to load
   */
  public void generateInstrLdc(String value) {
    short index = classFile.constantpool.newConstantString(value);
    if (0x00 <= index && index <= 0xFF) {
      generateInstrByte(INSTR_ldc, (byte) index, +1);
    } else {
      generateInstrShort(INSTR_ldc_w, index, +1);
    }
  }

  /**
   * Generates an ldc2 instruction (or an optimized form of it).
   * 
   * @param value  double constant to load
   */
  public void generateInstrLdc2(double value) {
    if (value == 0d) {
      generateInstr(INSTR_dconst_0, +2);
    } else if (value == 1d) {
      generateInstr(INSTR_dconst_1, +2);
    } else {
        generateInstrShort(INSTR_ldc2_w, 
          classFile.constantpool.newConstantDouble(value), +2);
    }
  }

  /**
   * Generates an ldc2 instruction (or an optimized form of it).
   * 
   * @param value  long constant to load
   */
  public void generateInstrLdc2(long value) {
    if (value == 0l) {
      generateInstr(INSTR_lconst_0, +2);
    } else if (value == 1l) {
      generateInstr(INSTR_lconst_1, +2);
    } else {
        generateInstrShort(INSTR_ldc2_w, 
          classFile.constantpool.newConstantLong(value), +2);
    }
  }

  /**
   * Generates an ldiv instruction.
   */
  public void generateInstrLdiv() {
    generateInstr(INSTR_ldiv, -2);
  }

  /**
   * Generates an lload instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load
   */
  public void generateInstrLload(short varnum) {
    generateLocalAccessInstr(INSTR_lload, INSTR_lload_0, varnum, +2);
  }

  /**
   * Generates an lmul instruction.
   */
  public void generateInstrLmul() {
    generateInstr(INSTR_lmul, -2);
  }

  /**
   * Generates an lneg instruction.
   */
  public void generateInstrLneg() {
    generateInstr(INSTR_lneg, 0);
  }

  /**
   * Generates a lookupswitch instruction.
   */
  public void generateInstrLookupswitch() {
    // TODO: needs to be implemented
  }

  /**
   * Generates an lor instruction.
   */
  public void generateInstrLor() {
    generateInstr(INSTR_lor, -2);
  }

  /**
   * Generates an lrem instruction.
   */
  public void generateInstrLrem() {
    generateInstr(INSTR_lrem, -2);
  }

  /**
   * Generates an lreturn instruction.
   */
  public void generateInstrLreturn() {
    generateInstr(INSTR_lreturn, -2);
  }

  /**
   * Generates an lshl instruction.
   */
  public void generateInstrLshl() {
    generateInstr(INSTR_lshl, -1);
  }

  /**
   * Generates an lshr instruction.
   */
  public void generateInstrLshr() {
    generateInstr(INSTR_lshr, -1);
  }

  /**
   * Generates an lstore instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to store
   */
  public void generateInstrLstore(short varnum) {
    generateLocalAccessInstr(INSTR_lstore, INSTR_lstore_0, varnum, -2);
  }

  /**
   * Generates an lsub instruction.
   */
  public void generateInstrLsub() {
    generateInstr(INSTR_lsub, -2);
  }

  /**
   * Generates an lushr instruction.
   */
  public void generateInstrLushr() {
    generateInstr(INSTR_lushr, -1);
  }

  /**
   * Generates an lxor instruction.
   */
  public void generateInstrLxor() {
    generateInstr(INSTR_lxor, -2);
  }

  /**
   * Generates a monitorenter instruction.
   */
  public void generateInstrMonitorenter() {
    generateInstr(INSTR_monitorenter, -1);
  }

  /**
   * Generates a monitorexit instruction.
   */
  public void generateInstrMonitorexit() {
    generateInstr(INSTR_monitorexit, -1);
  }

  /**
   * Generates a multianewarray instruction.
   *
   * @param dimensions  array dimensions
   * @param internalName  internal name of array element type
   */
  public void generateInstrMultianewarray(int dimensions, String internalName) {
    generateInstrShortByte(INSTR_multianewarray,
        classFile.constantpool.newConstantClass(internalName), (byte) dimensions, dimensions - 1);
  }

  /**
   * Generates a new instruction.
   * 
   * @param internalName  class name in internal format
   */
  public void generateInstrNew(String internalName) {
    generateInstrShort(INSTR_new, classFile.constantpool.newConstantClass(internalName), +1);
  }

  /**
   * Generates a newarray instruction.
   * 
   * @param internalName  signature of primitive type
   */
  @SuppressWarnings("fallthrough")
  public void generateInstrNewarray(String internalName) {
    Preconditions.checkArgument(internalName.length() == 1);

    byte atype = 0;
    switch (internalName.charAt(0)) {
      default:
        // COV_NF_START
        Compiler.internalError();
        break;
        // COV_NF_END

      case 'Z':
        atype = T_BOOLEAN;
        break;

      case 'B':
        atype = T_BYTE;
        break;

      case 'C':
        atype = T_CHAR;
        break;

      case 'S':
        atype = T_SHORT;
        break;

      case 'I':
        atype = T_INT;
        break;

      case 'J':
        atype = T_LONG;
        break;

      case 'F':
        atype = T_FLOAT;
        break;

      case 'D':
        atype = T_DOUBLE;
        break;
    }
    generateInstrByte(INSTR_newarray, atype, 0);
  }

  /**
   * Generates a nop instruction.
   */
  public void generateInstrNop() {
    generateInstr(INSTR_nop, 0);
  }

  /**
   * Generates a pop instruction.
   */
  public void generateInstrPop() {
    generateInstr(INSTR_pop, -1);
  }

  /**
   * Generates a pop2 instruction.
   */
  public void generateInstrPop2() {
    generateInstr(INSTR_pop2, -2);
  }

  /**
   * Generates a putfield instruction.
   * 
   * @param className  class name of field in internal format
   * @param name  field name
   * @param signature  field signature
   */
  public void generateInstrPutfield(String className, String name, String signature) {
    generateInstrShort(INSTR_putfield,
        classFile.constantpool.newConstantFieldref(className, name, signature),
        (short) (-getTypeSize(signature) - 1));
  }

  /**
   * Generates a putstatic instruction.
   * 
   * @param className  class name of field in internal format
   * @param name  field name
   * @param signature  field signature
   */
  public void generateInstrPutstatic(String className, String name, String signature) {
    generateInstrShort(INSTR_putstatic,
        classFile.constantpool.newConstantFieldref(className, name, signature),
        (short) -getTypeSize(signature));
  }

  /**
   * Generates a ret instruction (or an optimized form of it).
   * 
   * @param varnum  index of variable to load from
   */
  public void generateInstrRet(short varnum) {
    if (0x00 <= varnum && varnum <= 0xFF) {
      generateInstrByte(INSTR_ret, (byte) varnum, -1);
    } else {
      generateInstr(INSTR_wide, 0);
      generateInstrShort(INSTR_ret, varnum, -1);
    }
  }

  /**
   * Generates a return instruction.
   */
  public void generateInstrReturn() {
    generateInstr(INSTR_return, 0);
  }

  /**
   * Generates an saload instruction.
   */
  public void generateInstrSaload() {
    generateInstr(INSTR_saload, -1);
  }

  /**
   * Generates an sastore instruction.
   */
  public void generateInstrSastore() {
    generateInstr(INSTR_sastore, -3);
  }

  /**
   * Generates a swap instruction.
   */
  public void generateInstrSwap() {
    generateInstr(INSTR_swap, 0);
  }

  /**
   * Generates a tableswitch instruction.
   */
  public void generateInstrTableswitch() {
    // TODO: needs to be implemented
  }

  /**
   * Sets the label at the current code offset.
   * 
   * @param label  label to set
   */
  public void setLabel(Label label) {
    label.offset = buffer.getOffset();
  }

  /**
   * Creates a new label. Note that the label is not set yet!
   * 
   * @return  new label
   */
  public static Label newLabel() {
    return new Label();
  }


  /**
   * Generates an exception handler description.
   * 
   * @param startLabel  start label for code covered by handler
   * @param endLabel  end label for code covered by handler
   * @param handlerLabel  label of exception handler start
   * @param internalName  exception class name in internal format
   */
  public void generateExceptionHandlerInfo(Label startLabel, Label endLabel, Label handlerLabel,
      String internalName) {
    exceptionsAttributes.add(new ExceptionTableEntry(startLabel, endLabel, handlerLabel,
        classFile.constantpool.newConstantClass(internalName)));
  }

  /**
   * Generates line number debug information at the current code position.
   * 
   * @param line  line number (if 0 then no line number will be generated)
   */
  public void generateLineNumberInformation(short line) {
    if (line != 0) {
      if (lineNumberTableAttributeIndex == 0) {
        lineNumberTableAttributeIndex = classFile.constantpool.newConstantUtf8("LineNumberTable");
        attributeCount++;
      }
  
      lineNumbers.add(new LineNumberTableEntry((short) buffer.getOffset(), line));
    }
  }

  /**
   * Generates debug information for local variables
   * 
   * @param startLabel  label where variable becomes visible
   * @param endLabel  label where variable becomes in-visible
   * @param name  variable name
   * @param signature  variable type signature
   * @param variableIndex  variable slot index
   */
  public void generateLocalVariableInformation(Label startLabel, Label endLabel, String name,
      String signature, short variableIndex) {
    if (localVariableTableAttributeIndex == 0) {
      localVariableTableAttributeIndex =
          classFile.constantpool.newConstantUtf8("LocalVariableTable");
      attributeCount++;
    }

    localVariables.add(new LocalVariableTableEntry(startLabel, endLabel,
        classFile.constantpool.newConstantUtf8(name),
        classFile.constantpool.newConstantUtf8(signature), variableIndex));
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
   * Needs to be called before code for the method can be generated.
   */
  public void startCodeGeneration() {
    buffer = new Buffer();
    fixups = new ArrayList<Fixup>();
    exceptionsAttributes = new ArrayList<ExceptionTableEntry>();
    lineNumbers = new ArrayList<LineNumberTableEntry>();
    localVariables = new ArrayList<LocalVariableTableEntry>();
    codeAttributeIndex = classFile.constantpool.newConstantUtf8("Code");
  }

  /**
   * Needs to be called after all code for the method has been generated.
   */
  @SuppressWarnings("fallthrough")
  public void finishCodeGeneration() {
    // Perform needed fixups 
    for (Fixup fixup : fixups) {
      switch (fixup.kind) {
        default:
          // COV_NF_START
          Compiler.internalError();
          break;
          // COV_NF_END

        case Fixup.FIXUP_BRANCH:
          int offset = fixup.label.offset - fixup.offset;
          if (offset < Short.MIN_VALUE || Short.MAX_VALUE < offset) {
            throw new MethodTooBigException("branch exceeds maximum branch offset");
          } else {
            buffer.patch16(fixup.offset + 1, (short) offset);
          }
          break;
      }
    }

    // Copy generated code into a simple byte array
    bytecode = buffer.toByteArray();

    // Free unneeded data structures
    buffer = null;
    fixups = null;

    // Prune line numbers
    int lineNumbersCount = lineNumbers.size();
    if (lineNumbersCount > 0) {
      LineNumberTableEntry prevLineNumber = lineNumbers.get(0);
      List<LineNumberTableEntry> prunedLineNumbers = new ArrayList<LineNumberTableEntry>();
      for (int i = 1; i < lineNumbersCount; i++) {
        LineNumberTableEntry lineNumber = lineNumbers.get(i);
        if (prevLineNumber.offset == lineNumber.offset) {
          // Two adjacent entries have the same start offset: previous line didn't generate any
          // code, therefore omit it
          prevLineNumber = lineNumber;
        } else if (prevLineNumber.line == lineNumber.line) {
          // Two adjacent entries have the same line number: need to be merged, omit the current
          // entry
        } else {
          // Two adjacent entries are different: keep 'em
          prunedLineNumbers.add(prevLineNumber);
          prevLineNumber = lineNumber;
        }
      }
      prunedLineNumbers.add(prevLineNumber);
      lineNumbers = prunedLineNumbers;
    }
  }

  /**
   * Generates binary data for the method into its class file's data buffer.
   */
  protected void generate() {
    classFile.generate16(flags);
    classFile.generate16(nameIndex);
    classFile.generate16(signatureIndex);

    if ((flags & ACC_ABSTRACT) != 0) {
      // Abstract methods don't have any further attributes
      classFile.generate16((short) 0);
    } else {
      // Compute attribute lengths
      int exceptionAttributeLength = 2 + exceptionsAttributes.size() * (2 + 2 + 2 + 2);

      int lineNumberCount = lineNumbers.size();
      int lineNumberTableAttributeLength = (lineNumberCount == 0) ?
          0 :
          2 + 4 + 2 + lineNumberCount * (2 + 2);

      int localVariableCount = localVariables.size();
      int localVariableTableAttributeLength = (localVariableCount == 0) ?
          0 :
          2 + 4 + 2 + localVariableCount * (2 + 2 + 2 + 2 + 2);

      int runtimeVisibleAnnotationsAttributeLength = (runtimeVisibleAnnotationsAttribute == null) ?
          0 :
          runtimeVisibleAnnotationsAttribute.getLength();

      // Code attribute
      classFile.generate16((short) 1);
      classFile.generate16(codeAttributeIndex);
      classFile.generate32(2 + 2 + 4 + bytecode.length + exceptionAttributeLength + 2 +
          lineNumberTableAttributeLength + localVariableTableAttributeLength +
          runtimeVisibleAnnotationsAttributeLength);
      classFile.generate16((short) maxOpStackLevel);
      classFile.generate16((short) localsCount);
      classFile.generate32(bytecode.length);
      classFile.generateBytes(bytecode);

      // Any exception attributes
      classFile.generate16((short) exceptionsAttributes.size());
      for (ExceptionTableEntry ete : exceptionsAttributes) {
        ete.generate();
      }

      classFile.generate16(attributeCount);

      // Line number table attribute
      if (lineNumberCount > 0) {
        classFile.generate16(lineNumberTableAttributeIndex);
        classFile.generate32(lineNumberTableAttributeLength - (2 + 4));
        classFile.generate16((short) lineNumberCount);
        for (LineNumberTableEntry lnte : lineNumbers) {
          lnte.generate();
        }
      }

      // Local variable table attribute
      if (localVariableCount > 0) {
        classFile.generate16(localVariableTableAttributeIndex);
        classFile.generate32(localVariableTableAttributeLength - (2 + 4));
        classFile.generate16((short) localVariableCount);
        for (LocalVariableTableEntry lvte : localVariables) {
          lvte.generate();
        }
      }

      // RuntimeVisibleAnnotations attribute
      if (runtimeVisibleAnnotationsAttribute != null) {
        runtimeVisibleAnnotationsAttribute.generate();
      }
    }
  }
}
