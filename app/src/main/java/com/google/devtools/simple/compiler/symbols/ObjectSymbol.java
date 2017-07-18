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

package com.google.devtools.simple.compiler.symbols;

import com.google.devtools.simple.classfiles.ClassFile;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.classfiles.MethodTooBigException;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.Project;
import com.google.devtools.simple.compiler.RuntimeLoader;
import com.google.devtools.simple.compiler.expressions.AliasExpression;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.scopes.ObjectScope;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.symbols.synthetic.ConstructorSymbol;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.compiler.util.Signatures;
import com.google.devtools.simple.util.Files;
import com.google.devtools.simple.classfiles.ConstantPoolOverflowException;
import com.google.devtools.simple.compiler.types.ObjectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a symbol for objects (the equivalent of a Java class).
 *
 * @author Herbert Czymontek
 */
public class ObjectSymbol extends Symbol
        implements SymbolCanBePartOfQualifiedIdentifier, SymbolWithType {

    // Logging support
    private static final Logger LOG = Logger.getLogger(ObjectSymbol.class.getName());

    // Object type
    private Type type;

    // List of aliases defined inside the objects source
    private List<AliasExpression> aliases;

    // Namespace the object is defined in
    private NamespaceSymbol namespace;

    // Default constructor
    private ConstructorSymbol constructor;

    // List of data members defined by the object
    private List<DataMemberSymbol> dataMembers;

    // List of functions and procedures defined by the object
    private List<FunctionSymbol> functions;

    // List of properties defined by the object
    private List<PropertySymbol> properties;

    // List of event handlers defined by the object
    private Map<String, EventHandlerSymbol> eventHandlers;

    // List of events defined by the object
    private List<EventSymbol> events;

    // Base object of the object (Java equivalent is superclass)
    // Note that there is a difference to Java though: this will be null if there is no base object
    // while Java assumes to have at least java.lang.Object as a superclass.
    private ObjectType baseObject;

    // List of interfaces implemented by the object
    private List<ObjectType> interfaces;

    // Outer class of the object (only used for compiler generated objects)
    private ObjectSymbol outerclass;

    // List of inner classes of the object (only the compiler generates inner classes)
    private List<ObjectSymbol> innerclasses;

    // Object initialization event handler (can be null)
    private EventHandlerSymbol objectLoadEventHandler;

    // Instance initialization event handler (can be null)
    private EventHandlerSymbol instanceInitializeEventHandler;

    // Scope of the object
    private ObjectScope scope;

    // Indicates whether the object is an interface
    private boolean isInterface;

    // Indicates whether the object is a form
    private boolean isForm;

    /**
     * This factory method either returns the already existing symbol for the
     * object or creates a new one.
     *
     * @param compiler     current compiler instance
     * @param internalName internal name of object
     * @return object symbol
     */
    public static ObjectSymbol getObjectSymbol(Compiler compiler, String internalName) {
        String classname = Signatures.getInternalClassName(internalName);
        NamespaceSymbol namespace = NamespaceSymbol.getNamespaceSymbol(compiler,
                Signatures.getInternalPackageName(internalName));

        ObjectSymbol objectSymbol = (ObjectSymbol) namespace.getScope().lookupShallow(classname);
        if (objectSymbol == null) {
            objectSymbol = new ObjectSymbol(Scanner.NO_POSITION, classname, namespace, null);
            namespace.getScope().enterSymbol(objectSymbol);
        }
        return objectSymbol;
    }

    /**
     * Creates a new object symbol
     *
     * @param position   source code start position of symbol
     * @param name       object name
     * @param namespace  namespace to which object belongs
     * @param outerclass outer class of the object (must be {@code null} unless
     *                   object is compiler generated)
     */
    public ObjectSymbol(long position, String name, NamespaceSymbol namespace,
                        ObjectSymbol outerclass) {
        super(position, name);

        this.namespace = namespace;
        this.outerclass = outerclass;

        aliases = new ArrayList<AliasExpression>();

        dataMembers = new ArrayList<DataMemberSymbol>();
        functions = new ArrayList<FunctionSymbol>();
        properties = new ArrayList<PropertySymbol>();
        eventHandlers = new HashMap<String, EventHandlerSymbol>();
        events = new ArrayList<EventSymbol>();
        innerclasses = new ArrayList<ObjectSymbol>();
        interfaces = new ArrayList<ObjectType>();

        scope = new ObjectScope(this,
                outerclass != null ? outerclass.getScope() : namespace.getScope());
        type = new ObjectType(this);

        if (outerclass != null) {
            outerclass.addInnerclass(this);
        }
    }

    /**
     * Sets the base object (same as superclass in Java).
     *
     * @param baseObject base object
     */
    public void setBaseObject(ObjectType baseObject) {
        this.baseObject = baseObject;
    }

    /**
     * Marks the object as an interface object.
     */
    public void markAsInterface() {
        isInterface = true;
    }

    /**
     * Marks the class as being compiled (as opposed to be being loaded from a
     * classfile.
     * 标志类被编译(而不是从一个被加载　　*类文件。
     */
    public void markAsCompiled() {
        // This just requires generation of a default constructor
        constructor = new ConstructorSymbol(this);
    }

    /**
     * Adds an alias definition made in the object's source file.
     *
     * @param alias alias definition
     */
    public void addAlias(AliasExpression alias) {
        // TODO: check for re-alias? Warning?
        // TODO: is it OK for an alias to hide a member?
        aliases.add(alias);
    }

    /**
     * Adds a new data member to the object.
     *
     * @param dataMember data member symbol to be added
     * @return {@code true} after successfully added the member, {@code false}
     * if another member with the same name was already defined in the
     * object
     */
    public boolean addDataMember(DataMemberSymbol dataMember) {
        // All names must be unique within an object
        if (scope.lookupShallow(dataMember.getName()) != null) {
            return false;
        }

        dataMembers.add(dataMember);
        scope.enterSymbol(dataMember);
        return true;
    }

    /**
     * Adds a new function to the object.
     *
     * @param function function symbol to be added
     * @return {@code true} after successfully added the member, {@code false}
     * if another member with the same name was already defined in the
     * object
     */
    public boolean addFunction(FunctionSymbol function) {
        // All names must be unique within an object
        if (scope.lookupShallow(function.getName()) != null) {
            return false;
        }

        functions.add(function);
        scope.enterSymbol(function);
        return true;
    }

    /**
     * Adds a new property to the object.
     *
     * @param property property symbol to be added
     * @param getter   getter function for the property or {@code null} if there
     *                 is no getter function
     * @param setter   setter procedure for the property or {@code null} if there
     *                 is no setter function
     * @return {@code true} after successfully added the member, {@code false}
     * if another member with the same name was already defined in the
     * object
     */
    public boolean addProperty(PropertySymbol property, FunctionSymbol getter,
                               FunctionSymbol setter) {
        // All names must be unique within an object
        if (scope.lookupShallow(property.getName()) != null) {
            return false;
        }

        properties.add(property);
        scope.enterSymbol(property);

        if (getter != null) {
            functions.add(getter);
        }

        if (setter != null) {
            functions.add(setter);
        }

        return true;
    }

    /**
     * Adds a new event definition to the object.
     *
     * @param event event symbol to be added
     * @return {@code true} after successfully adding the member, {@code false}
     * if another member with the same name was already defined in the
     * object
     */
    public boolean addEvent(EventSymbol event) {
        // All names must be unique within an object
        if (scope.lookupShallow(event.getName()) != null) {
            return false;
        }

        events.add(event);
        functions.add(event);

        scope.enterSymbol(event);
        return true;
    }

    /**
     * Adds a new event handler definition to the object.
     *
     * @param eventHandler event handler symbol to be added
     * @return {@code true} after successfully adding the member, {@code false}
     * if another event handler with the same name was already defined
     * in the object
     */
    public boolean addEventHandler(EventHandlerSymbol eventHandler) {
        String combinedName = eventHandler.getEventTargetName() + '.' + eventHandler.getName();
        if (eventHandlers.containsKey(combinedName)) {
            return false;
        }

        eventHandlers.put(combinedName, eventHandler);
        functions.add(eventHandler);
        return true;
    }

    /**
     * Adds an object as an inner class to this object. Note that only compiler
     * generated objects can be inner classes.
     *
     * @param innerclass inner class object
     */
    protected void addInnerclass(ObjectSymbol innerclass) {
        innerclasses.add(innerclass);
    }

    /**
     * Adds an object type as an interface to be implemented by this object.
     *
     * @param in interface to be implemented
     */
    public void addInterface(ObjectType in) {
        // TODO: warning on re-implementation?
        interfaces.add(in);
    }

    /**
     * Sets an event handler to be invoked upon object loading (invoked from
     * <clinit>).
     *
     * @param eventHandler object load event handler
     */
    public void setObjectLoadEventHandler(EventHandlerSymbol eventHandler) {
        objectLoadEventHandler = eventHandler;
    }

    /**
     * Sets an event handler to be invoked upon instance creation (invoked from
     * <init>).
     *
     * @param eventHandler instance initialization event handler
     */
    public void setInstanceInitializeEventHandler(EventHandlerSymbol eventHandler) {
        instanceInitializeEventHandler = eventHandler;
    }

    /**
     * Returns the instance initialization event handler.
     *
     * @return instance initialization event handler
     */
    public EventHandlerSymbol getInstanceInitializeEventHandler() {
        return instanceInitializeEventHandler;
    }

    /**
     * Returns the default constructor for the object.
     *
     * @return default constructor
     */
    public ConstructorSymbol getConstructor() {
        return constructor;
    }

    /**
     * Indicates whether the object was loaded from a class file or being
     * compiled.
     *
     * @return {@code true} if object was loaded from a class file, {@code
     * false} otherwise
     */
    public boolean isLoaded() {
        return constructor == null;
    }

    /**
     * Indicates whether the object is an interface object.
     *
     * @return {@code true} if the object is an interface object, {@code false}
     * otherwise
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Indicates whether the object is a form.
     *
     * @return {@code true} if the object is a form, {@code false} otherwise
     */
    public boolean isForm() {
        return isForm;
    }

    /**
     * Returns the base object of the object.
     *
     * @return base object or {@code null} if there is no base object
     */
    public ObjectType getBaseObject() {
        return baseObject;
    }

    /**
     * Returns the outer class of the object. Only compiler generated objects can
     * have an outer class.
     *
     * @return outer class or {@code null}
     */
    public ObjectSymbol getOuterclass() {
        return outerclass;
    }

    /**
     * Returns a list of interfaces implemented by the object.
     *
     * @return list of interfaces (never {@code null} but can be empty of
     * course)
     */
    public List<ObjectType> getInterfaces() {
        return interfaces;
    }

    /**
     * Returns a list of data members defined by the object.
     *
     * @return list of data members (never {@code null} but can be empty of
     * course)
     */
    public List<DataMemberSymbol> getDataMembers() {
        return dataMembers;
    }

    /**
     * Returns a list of functions defined by the object.
     *
     * @return list of functions (never {@code null} but can be empty of
     * course)
     */
    public List<FunctionSymbol> getFunctions() {
        return functions;
    }

    /**
     * Returns a list of events defined by the object.
     *
     * @return list of events (never {@code null} but can be empty of
     * course)
     */
    public List<EventSymbol> getEvents() {
        return events;
    }

    /**
     * Returns a list of events handlers defined in the object.
     *
     * @return list of event handlers
     */
    public Collection<EventHandlerSymbol> getEventHandlers() {
        return eventHandlers.values();
    }

    @Override
    public ObjectScope getScope() {
        return scope;
    }

    /**
     * Returns the namespace the object belongs to.
     *
     * @return namespace symbol
     */
    public NamespaceSymbol getNamespace() {
        return namespace;
    }

    /**
     * Returns a name for the object. For compiler generated objects a unique
     * name is generated.
     *
     * @return name for the object
     */
    public String getObjectName() {
        StringBuilder objectName = new StringBuilder();
        if (outerclass != null) {
            objectName.append(outerclass.getObjectName());
            objectName.append('$');
        }

        if (getName() == null) {
            objectName.append(outerclass.innerclasses.indexOf(this));
        } else {
            objectName.append(getName());
        }

        return objectName.toString();
    }

    @Override
    public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
        // Before we can start resolution we need to enter alias symbols into the symbol table
        Scope globalScope = compiler.getGlobalNamespaceSymbol().getScope();
        for (AliasExpression alias : aliases) {
            alias.resolve(compiler, null);
            globalScope.enterSymbol(alias.getAliasSymbol());
        }

        // Resolve base object first
        if (baseObject != null) {
            baseObject.resolve(compiler);
        }

        // Followed by interface type
        for (ObjectType interfaceType : interfaces) {
            interfaceType.resolve(compiler);
        }

        // And generated inner classes
        for (ObjectSymbol innerclass : innerclasses) {
            innerclass.resolve(compiler, null);
        }

        // Finally resolve data members, properties, events and functions (but not their bodies yet!)
        for (DataMemberSymbol dataMember : dataMembers) {
            dataMember.resolve(compiler, currentFunction);
        }
        for (PropertySymbol property : properties) {
            property.resolve(compiler, currentFunction);
        }
        for (EventSymbol event : events) {
            event.resolve(compiler, null);
        }
        for (FunctionSymbol function : functions) {
            function.resolve(compiler, null);
        }

        isForm = scope.lookupShallow("$define") != null;

        // Resolve constructor (if the object has one)
        if (constructor != null) {
            constructor.resolve(compiler, null);
        }

        // And don't forget to remove alias symbols again (they are defined locally to this object
        // and must linger around and be picked up by other objects)
        for (AliasExpression alias : aliases) {
            globalScope.removeSymbol(alias.getAliasSymbol());
        }

        // Check to see if this symbol references a component class that requires Android permissions
        compiler.checkForPermissions(this);
    }

    /**
     * Resolves the function bodies of the functions defined by the object.
     *
     * @param compiler current compiler instance
     */
    public void resolveFunctionBodies(Compiler compiler) {
        // Before we can start resolution we need to enter alias symbols into the symbol table
        Scope globalScope = compiler.getGlobalNamespaceSymbol().getScope();
        for (AliasExpression alias : aliases) {
            globalScope.enterSymbol(alias.getAliasSymbol());
        }

        // Resolve function bodies
        for (FunctionSymbol function : functions) {
            function.resolveBody(compiler);
        }

        // And don't forget to remove alias symbols again (they are defined locally to this object
        // and must linger around and be picked up by other objects)
        for (AliasExpression alias : aliases) {
            globalScope.removeSymbol(alias.getAliasSymbol());
        }
    }

    @Override
    public void generateRead(Method m) {
        // This happens for qualified identifiers of static data members or functions
    }

    @Override
    public void generateWrite(Method m) {
        // This happens for qualified identifiers of static data members
    }

    /**
     * Generates a class file for the object.
     *
     * @param compiler       current compiler instance
     * @param buildDirectory target root directory for class file
     */
    public void generate(Compiler compiler, File buildDirectory) {
        writeClassFile(compiler, generate(compiler), buildDirectory);
    }

    /**
     * Generates a classfile in-memory and adds the classfile to the given list.
     *
     * @param compiler current compiler instance
     * @param classes  list of generated classfiles
     */
    public void generate(Compiler compiler, List<ClassFile> classes) {
        classes.add(generate(compiler));
    }

    /**
     * Writes the generated classfile for the object into its target directory.
     *
     * @param compiler       current compiler instance
     * @param cf             classfile
     * @param buildDirectory target root directory
     */
    protected void writeClassFile(Compiler compiler, ClassFile cf, File buildDirectory) {
        File classFile = new File(namespace.generate(buildDirectory), getObjectName() + ".class");
        try {
            // Generate classfile data and write classfile
            Files.write(cf.generate(), classFile);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Classfile write error", e);
            compiler.error(Scanner.NO_POSITION, Error.errWriteError, classFile.getName());
        }
    }

    /*
     * Generates a class initializer which will raise an object Load() event as well as initialize
     * statically sized data member arrays.
     */
    private void generateClassInitializer(ClassFile cf) {
        Method m = cf.newMethod((short) (Method.ACC_SYNTHETIC | Method.ACC_PUBLIC), "<clinit>", "()V");
        try {
            m.startCodeGeneration();

            // Initialize statically sized data members
            for (DataMemberSymbol dataMember : dataMembers) {
                if (dataMember.getClass() == ObjectDataMemberSymbol.class) {
                    dataMember.generateInitializer(m);
                }
            }

            // Raise object load event
            if (objectLoadEventHandler != null) {
                m.generateInstrInvokestatic(getType().internalName(),
                        objectLoadEventHandler.getName(), "()V");
            }

            m.generateInstrReturn();
            m.finishCodeGeneration();
        } catch (MethodTooBigException e) {
            Compiler.internalError();  // COV_NF_LINE
        }
    }

    /**
     * Returns internal name of base object type.
     *
     * @param compiler current compiler instance
     * @return internal name of base object type
     */
    public String getBaseObjectInternalName(Compiler compiler) {
        if (baseObject == null) {
            return "java/lang/Object";
        }

        // We need to special case for form here (because it is a component)
        return isForm() ?
                compiler.getComponentImplementationInternalName(baseObject) :
                baseObject.getType().internalName();
    }

    /*
     * Generates the Java class for the object.
     */
    private ClassFile generate(Compiler compiler) {
        short flags = ClassFile.ACC_PUBLIC;
        if (isInterface()) {
            flags |= ClassFile.ACC_INTERFACE | ClassFile.ACC_ABSTRACT;
        } else {
            flags |= ClassFile.ACC_SUPER;
        }

        String internalName = getType().internalName();
        String baseObjectInternalName = getBaseObjectInternalName(compiler);

        compiler.addClassfile(internalName);

        ClassFile cf = new ClassFile(flags, internalName, baseObjectInternalName);
        try {
            for (ObjectType interfaceType : interfaces) {
                cf.addInterfaceImplementation(interfaceType.internalName());
            }

            for (DataMemberSymbol dataMember : dataMembers) {
                dataMember.generate(cf);
            }

            generateClassInitializer(cf);

            for (FunctionSymbol function : functions) {
                function.generate(compiler, cf);
            }

            if (!isInterface) {
                constructor.generate(compiler, cf);
            }

            for (ObjectSymbol innerclass : innerclasses) {
                cf.addInnerClass(ClassFile.ACC_PUBLIC, innerclass.getName(),
                        innerclass.getType().internalName());
            }

            if (outerclass != null) {
                cf.addOuterClass(ClassFile.ACC_PUBLIC, outerclass.getName(),
                        outerclass.getType().internalName());
            }

            cf.getRuntimeVisibleAnnotationsAttribute().newAnnotation(
                    'L' + RuntimeLoader.ANNOTATION_INTERNAL + "/SimpleObject;");

            cf.setSourceFile(getName() + Project.SOURCEFILE_EXTENSION);
        } catch (ConstantPoolOverflowException e) {
            compiler.error(Scanner.NO_POSITION, Error.errConstantPoolOverflow,
                    internalName.replace('/', '.'));
        }

        return cf;
    }

    // SymbolWithType implementation

    public final Type getType() {
        return type;
    }

    public final void setType(Type type) {
        this.type = type;
    }
}
