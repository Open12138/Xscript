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

import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.scopes.synthetic.ErrorScope;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.types.synthetic.ErrorType;

/**
 * Represents object types.
 * 
 * @author Herbert Czymontek
 */
public class ObjectType extends Type implements TypeWithScope {

  /**
   * Instance of an unspecified object type. For internal use only! 
   */
  public static final ObjectType objectType = new ObjectType(null);

  // Internal name of runtime support class for object variants
  private static final String OBJECT_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/ObjectVariant";

  // Internal name of runtime support class for Object reference parameters
  private static final String OBJECT_REFERENCE_INTERNAL_NAME =
      REFERENCE_PACKAGE_INTERNAL_NAME + "/ObjectReferenceParameter";

  // Corresponding object symbol
  protected ObjectSymbol objectSymbol;

  /**
   * Creates a new object type.
   * 
   * @param objectSymbol  corresponding object symbol
   */
  public ObjectType(ObjectSymbol objectSymbol) {
    super(TypeKind.OBJECT);
    this.objectSymbol = objectSymbol;
  }

  @Override
  public boolean isObjectType() {
    return true;
  }

  @Override
  public Type getType() {
    // objectSymbol can be null in case an object cannot successfully be resolved
    return objectSymbol == null ? ErrorType.errorType : objectSymbol.getType();
  }

  /**
   * Returns the corresponding object symbol for the type.
   * 
   * @return  object symbol
   */
  public ObjectSymbol getObjectSymbol() {
    return objectSymbol;
  }

  /**
   * Checks whether an object is a base object of this type.
   * 
   * @param baseObject  potential base object
   * @return  {@code true} if it is a base object, {@code false} otherwise
   */
  public boolean isBaseObject(ObjectType baseObject) {
    if (equals(baseObject)) {
      return true;
    }

    ObjectType thisBaseObject = getObjectSymbol().getBaseObject();
    return thisBaseObject == null ? false : thisBaseObject.isBaseObject(baseObject);
  }

  /**
   * Checks whether an object implements an interface.
   * 
   * @param interfaceType  interface to implement
   * @return  {@code true} if the interface is implemented, {@code false}
   *          otherwise
   */
  public boolean implementsInterface(ObjectType interfaceType) {
    // TODO: interfaces extending other interfaces...

    for (ObjectType in : getObjectSymbol().getInterfaces()) {
      if (in.equals(interfaceType)) {
        return true;
      }
    }

    ObjectType thisBaseObject = getObjectSymbol().getBaseObject();
    return thisBaseObject == null ? false : thisBaseObject.implementsInterface(interfaceType);
  }

  public Scope getScope() {
    // objectSymbol can be null in case an object cannot successfully be resolved
    return objectSymbol == null ? new ErrorScope() : objectSymbol.getScope();
  }

  @Override
  public boolean canConvertTo(Type toType) {
    return toType.isObjectType() || toType.isVariantType();
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrAconstNull();
  }

  @Override
  public void generateConversion(Method m, Type toType) {
    if (toType.isVariantType()) {
      m.generateInstrInvokestatic(OBJECT_VARIANT_INTERNAL_NAME, "getObjectVariant",
          "(Ljava/lang/Object;)L" + OBJECT_VARIANT_INTERNAL_NAME + ";");
    } else {
      m.generateInstrCheckcast(toType.internalName());
    }
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrAload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrAstore(varIndex);
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrAaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrAastore();
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrAreturn();
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrIfAcmpeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrIfAcmpne(label);
  }

  @Override
  public void generateBranchIfCmpIdentical(Method m, Method.Label label) {
    m.generateInstrIfAcmpeq(label);
  }

  @Override
  public void generateBranchIfCmpNotIdentical(Method m, Method.Label label) {
    m.generateInstrIfAcmpne(label);
  }

  @Override
  public void generateTypeOf(Method m, String internalName) {
    m.generateInstrInstanceof(internalName);
  }

  @Override
  public String internalName() {
    return objectSymbol == null ?
        "java/lang/Object" :
        objectSymbol.getNamespace().internalName() + objectSymbol.getObjectName();
  }

  @Override
  public String referenceInternalName() {
    return OBJECT_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "Ljava/lang/Object;";
  }

  @Override
  public String signature() {
    return 'L' + internalName() + ';';
  }
  
  @Override
  public String toString() {
    return internalName().replace('/', '.');
  }
}
