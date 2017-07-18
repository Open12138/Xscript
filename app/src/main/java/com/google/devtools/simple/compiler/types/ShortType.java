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
 * Represents the primitive Short type.
 * 
 * @author Herbert Czymontek
 */
public final class ShortType extends SmallIntegerType {

  /**
   * Instance of primitive Short type. 
   */
  public static final ShortType shortType = new ShortType();

  // Internal name of runtime support class for Short variants
  private static final String SHORT_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/ShortVariant";

  // Internal name of runtime support class for Short reference parameters
  private static final String SHORT_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/ShortReferenceParameter";

  /*
   * Creates a new Short type.
   */
  private ShortType() {
    super(TypeKind.SHORT);
  }

  @Override
  public int bitsize() {
    return 16;
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
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "short2byte", "(S)B");
        break;
  
      case SHORT:
      case INTEGER:
        break;
  
      case LONG:
        m.generateInstrI2l();
        break;
  
      case SINGLE:
        m.generateInstrI2f();
        break;
  
      case DOUBLE:
        m.generateInstrI2d();
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(SHORT_VARIANT_INTERNAL_NAME, "getShortVariant",
            "(S)L" + SHORT_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokestatic("java/lang/Short", "toString", "(S)Ljava/lang/String;");
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrSaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrSastore();
  }

  @Override
  public String internalName() {
    return "S";
  }

  @Override
  public String referenceInternalName() {
    return SHORT_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "S";
  }

  @Override
  public String toString() {
    return "Short";
  }
}
