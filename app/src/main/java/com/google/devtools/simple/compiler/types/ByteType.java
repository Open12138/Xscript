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

package com.google.devtools.simple.compiler.types;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.classfiles.Method;

/**
 * Represents the primitive Byte type.
 * 
 * @author Herbert Czymontek
 */
public final class ByteType extends SmallIntegerType {

  /**
   * Instance of primitive Byte type. 
   */
  public static final ByteType byteType = new ByteType();

  // Internal name of runtime support class for Byte variants
  private static final String BYTE_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/ByteVariant";

  // Internal name of runtime support class for Byte reference parameters
  private static final String BYTE_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/ByteReferenceParameter";

  /*
   * Creates a new Byte type.
   */
  private ByteType() {
    super(TypeKind.BYTE);
  }

  @Override
  public int bitsize() {
    return 8;
  }

  private void generateMakeByteSigned(Method m) {
    m.generateInstrI2b();
    m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "byte2integer", "(B)I");
  }

  @Override
  public void generateConversion(Method m, Type toType) {
    switch (toType.getKind()) {
      default:
        // COV_NF_START
        Compiler.internalError();
        break;
        // COV_NF_END
  
      case BOOLEAN:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "integer2boolean", "(I)Z");
        break;
  
      case BYTE:
        break;
  
      case SHORT:
      case INTEGER:
        // Java bytes are unsigned, but all Simple numeric types are signed
        generateMakeByteSigned(m);
        break;
  
      case LONG:
        generateMakeByteSigned(m);
        m.generateInstrI2l();
        break;
  
      case SINGLE:
        generateMakeByteSigned(m);
        m.generateInstrI2f();
        break;
  
      case DOUBLE:
        generateMakeByteSigned(m);
        m.generateInstrI2d();
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(BYTE_VARIANT_INTERNAL_NAME, "getByteVariant",
            "(B)L" + BYTE_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    generateMakeByteSigned(m);
    m.generateInstrInvokestatic("java/lang/Integer", "toString", "(I)Ljava/lang/String;");
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrBaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrBastore();
  }

  @Override
  public void generateJavaBox(Method m) {
    m.generateInstrInvokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
  }

  @Override
  public String internalName() {
    return "B";
  }

  @Override
  public String referenceInternalName() {
    return BYTE_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "B";
  }

  @Override
  public String toString() {
    return "Byte";
  }
}
