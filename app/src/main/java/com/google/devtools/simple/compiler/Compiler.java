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

package com.google.devtools.simple.compiler;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.android.dx.command.DxConsole;
import com.android.dx.command.dexer.Main.Arguments;
import com.android.dx.dex.code.PositionList;
import com.android.sdklib.build.ApkBuilderMain;
import com.google.devtools.simple.classfiles.ClassFile;
import com.google.devtools.simple.compiler.parser.Parser;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.symbols.NamespaceSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.util.Signatures;
import com.google.devtools.simple.util.Execution;
import com.google.devtools.simple.util.Files;
import com.wolf.util.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main entry point for the Simple compiler.
 * <p/>
 * <p>Supplies entry points for building and deploying Simple projects.
 *
 * @author Herbert Czymontek
 */
public final class Compiler {
    //jar库里面必须有一个 .xruntime后缀的描述文件  文件内容为以 # 分开的所有类名
    public static final String ORIGINAL = ".xruntime";

    // Executable file extension
    private static final String EXECUTABLE_EXTENSION = "";
//            System.getProperty("os.name").startsWith("Windows") ? ".exe" : "";

    // File names and locations
    private static final String JAVA_HOME = System.getenv("JAVA_HOME");
    private static final String ANDROID_HOME = System.getenv("ANDROID_HOME");
    private static final String SIMPLE_HOME = System.getenv("SIMPLE_HOME");

    private static final String JARSIGNER_BINARY =
            JAVA_HOME + "/bin/jarsigner" + EXECUTABLE_EXTENSION;
    private static final String KEYTOOL_BINARY =
            JAVA_HOME + "/bin/keytool" + EXECUTABLE_EXTENSION;
//    private static final String AAPT_BINARY =
//            ANDROID_HOME + "/platforms/android-1.5/tools/aapt" + EXECUTABLE_EXTENSION;

//    private static final String ANDROID_RUNTIME = ANDROID_HOME + "/platforms/android-1.5/android.jar";
//    private static final String SIMPLE_ANDROID_RUNTIME = SIMPLE_HOME + "/SimpleAndroidRuntime.jar";

    // Keystore for signing apk files
    private static final String ANDROID_DEBUG_KEYSTORE = "android_debug.keystore";

    /**
     * Target compilation platforms.
     */
    public enum Platform {

        None(""),
        Android(XInit.XRunTime);

        // Simple runtime library
        private final String runtimeLibrary;

        Platform(String runtimeLibrary) {
            this.runtimeLibrary = runtimeLibrary;
        }

        public String getRuntimeLibrary() {
            return runtimeLibrary;
        }
    }

    /**
     * Root package name of the Simple runtime. All runtime library packages are
     * subpackages of this package.
     * 根包的名字简单的运行时。所有的运行时库包都是这个包的子包。
     */
    public static final String RUNTIME_ROOT_PACKAGE = "com.google.devtools.simple.runtime";
    public static final String RUNTIME_ROOT_INTERNAL = RUNTIME_ROOT_PACKAGE.replace('.', '/');

    /**
     * Internal name of runtime error superclass
     */
    public static final String RUNTIME_ERROR_INTERNAL_NAME =
            RUNTIME_ROOT_INTERNAL + "/errors/RuntimeError";

    // Logging support
    private static final Logger LOG = Logger.getLogger(Compiler.class.getName());

    // stdout and stderr streams to be used by the compiler
    private PrintStream out;
    private PrintStream err;

    // Character encoding of source files
    private String encoding;

    // Target platform
    private Platform platform;

    // List of objects being compiled
    private List<ObjectSymbol> objects;

    // List of generated classfiles
    private List<String> classfiles;

    // Number of compilation errors and warnings
    private int errorCount;
    private int warningCount;

    // Global (unnamed) namespace symbol
    private NamespaceSymbol globalNamespaceSymbol;

    // Runtime error type
    private final ObjectType runtimeErrorType;

    // File index to filename mapping
//    文件索引 --文件名 映射
    private final List<String> filenameMap;

    // Map from object symbols of components needing Android permissions to the corresponding class
    // Note that this included components that may not be referenced by the currently compiling
    // package
    private final Map<ObjectSymbol, Class<?>> symbolToClassNeedingPermissionsMap;

    // Set of component classes referenced by the current package that require Android permissions
    private Set<Class<?>> classesNeedingPermissions;

    // Runtime library loader and analyzer
    private RuntimeLoader runtimeLoader;

    /**
     * Add a component class to the list of Simple component classes that require Android permissions.
     *
     * @param usesPermissionsClass       the component class specifying the required permission.
     * @param usesPermissionObjectSymbol the {@link ObjectSymbol} corresponding to usesPermissionClass
     */
    public void addToPermissions(Class<?> usesPermissionsClass,
                                 ObjectSymbol usesPermissionObjectSymbol) {
        symbolToClassNeedingPermissionsMap.put(usesPermissionObjectSymbol, usesPermissionsClass);
    }

    /**
     * Check to see if this object symbol corresponds to a Simple component class that requires
     * Android permissions and add it to the set of such classes.
     *
     * @param objectSymbol the object symbol to check
     */
    public void checkForPermissions(ObjectSymbol objectSymbol) {
        if (symbolToClassNeedingPermissionsMap.containsKey(objectSymbol)) {
            classesNeedingPermissions.add(symbolToClassNeedingPermissionsMap.get(objectSymbol));
        }
    }

    /*
     * Generate the set of Android permissions needed by this package.
     *
     * Note that we do a bunch of reflection here rather than directly using the
     * {@link UsesPermissions} class and its methods. This is because the {@link UsesPermissions}
     * class that would be directly referenced here is actually a different class object than the one
     * created for the component annotations. This is due to them being from different class
     * loaders. Consequently, attempts to do things like 'getAnnotation(UsesPermissions.class)' or
     * to cast the annotation objects to UsesPermissions all failed.
     */
    private Set<String> generatePermissions() {
        Set<String> permissions = new HashSet<String>();
        final Class<? extends Annotation> usesPermissionAnnotationClass =
                runtimeLoader.getAndroidUsesPermission();
        java.lang.reflect.Method permissionNameMethod = null;
        try {
            permissionNameMethod = usesPermissionAnnotationClass.getMethod("permissionNames");
        } catch (NoSuchMethodException e) {
            LOG.log(Level.SEVERE,
                    "Simple compiler doesn't know about UsesPermissions's permissionNames method.");
        }
        for (Class<?> classNeedingPermission : classesNeedingPermissions) {
            final Annotation usesPermissionsAnnotation =
                    classNeedingPermission.getAnnotation(usesPermissionAnnotationClass);
            try {
                if (usesPermissionsAnnotation != null) {
                    final String permissionsString =
                            (String) permissionNameMethod.invoke(usesPermissionsAnnotation);
                    if (permissionsString != null && permissionsString.length() > 0) {
                        final String[] permissionStrings = permissionsString.split(",");
                        for (String permissionString : permissionStrings) {
                            permissions.add(permissionString.trim());
                        }
                    }
                } else {
                    LOG.log(Level.SEVERE, "Class doesn't have UsesPermissions annotation: "
                            + classNeedingPermission.getName());
                }
            } catch (InvocationTargetException e) {
                LOG.log(Level.SEVERE,
                        "Simple compiler doesn't know how to deal with UsesPermissions annotation.");
            } catch (IllegalAccessException e) {
                LOG.log(Level.SEVERE,
                        "Simple compiler doesn't know how to deal with UsesPermissions annotation.");
            }
        }
        return permissions;
    }

    /*
     * Creates an AndroidManifest.xml file needed for deploying the built Android application.
     */
    private static boolean writeAndroidManifest(Compiler compiler, Project project,
                                                File buildDirectory, Set<String> permissionsNeeded) {
        // Create AndroidManifest.xml
        String mainForm = project.getMainForm();
        File manifest = new File(buildDirectory, "AndroidManifest.xml");
        String pname1 = project.getXPackagename();
        if (pname1.equals("") || pname1 == null) {
            pname1 = Signatures.getPackageName(mainForm);
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(manifest));
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            out.write("<manifest " +
                    "xmlns:android=\"http://schemas.android.com/apk/res/android\" " +
                    "android:versionCode=\"" + project.getVersionCode() + "\" " +
                    "android:versionName=\"" + project.getVersionName() + "\" " +
                    "package=\"" + pname1 + "\">\n");
            for (String permission : permissionsNeeded) {
                out.write("   <uses-permission android:name=\"" + permission + "\" />\n");
            }
            String icon;
            if (project.getIconPath() == null)
                icon = "ic_launcher";
            else
                icon = project.getIconPath().substring(project.getIconPath().lastIndexOf(File.separator) + 1, project.getIconPath().lastIndexOf("."));
            out.write("   <application " +
                    "android:icon=\"@drawable/" + icon + "\" " +
                    "android:theme=\"@android:style/Theme.Holo.Light.NoActionBar\" " +
                    "android:label=\"" + project.getProjectName()
                    + "\" >\n");
            out.write("     <activity " +
                    "android:name=\"com.google.devtools.simple.runtime.android.ApplicationImpl\" " +
                    "android:label=\"" + project.getProjectName() + "\">\n");
            out.write("         <meta-data " +
                    "android:name=\"com.google.devtools.simple.runtime.android.MainForm\" " +
                    "android:value=\"" + mainForm + "\" />\n");
            out.write("         <intent-filter>\n");
            out.write("            <action android:name=" +
                    "\"android.intent.action.MAIN\"/>\n");
            out.write("            <category android:name=" +
                    "\"android.intent.category.LAUNCHER\"/>\n");
            out.write("         </intent-filter>\n");
            out.write("      </activity>\n");
            out.write("   </application>\n");
            out.write("</manifest>\n");
            out.close();
            return true;
        } catch (IOException e) {
            compiler.error(Scanner.NO_POSITION, Error.errWriteError, manifest.toString());
            return false;
        }
    }

    /**
     * Should be called to indicate an unexpected situation.
     */
    public static void internalError() {
        Main.err("Internal error");
        throw new IllegalStateException("Internal error");
    }


    public static String outpath = "";

    /**
     * Builds a Simple project.
     *
     * @param platform target platform to build for
     * @param project  project to build
     * @param out      standard output stream to redirect to
     * @param err      standard error stream to redirect to
     * @return {@code true} if the compilation succeeds, {@code false} otherwise
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean compile(Platform platform, Project project, PrintStream out,
                                  PrintStream err, Context context) {
        //输出apk路径
        outpath = "";
        // Create a new compiler instance for the compilation
        long start = System.currentTimeMillis();
        final Compiler compiler = new Compiler(platform, out, err, context);

        // Parse all source files .sample
        for (Project.SourceDescriptor arg : project.getSources()) {
            String fileName = arg.getQualifiedName().replace('.', '/') + Project.SOURCEFILE_EXTENSION;
            out.println("________Compiling " + fileName);
            Utils.log("________Compiling " + fileName);
            Main.state("________正在编译 " + fileName);
            try {
                new Parser(compiler,
                        new Scanner(compiler, compiler.createFileIndex(fileName), Files.read(arg.getFile(), compiler.encoding)),
                        arg.getQualifiedName())
                        .parse();
            } catch (IOException ioe) {
                compiler.error(Scanner.NO_POSITION, Error.errReadError, arg.getFile().toString());
            }
        }
        Utils.log("parseok");
        Main.state("_______编译完成");
        // Resolve symbol information in parse trees
        compiler.resolve();

        // Create and package class files if there were no compilation errors
        if (compiler.errorCount > 0) {
            compiler.out.println("Compilation errors: " + compiler.errorCount);
            Main.err("编译错误个数: " + compiler.errorCount);
        } else {
            switch (compiler.platform) {
                default:
                    Compiler.internalError();
                    return false; // Will never get here...

                case Android:
                    Main.state("初始化安卓构建目录");
                    // Create build directories if they do not exist
                    File buildDir = Files.createDirectory(project.getBuildDirectory());
                    File tmpDir = Files.createDirectory(buildDir, "tmp");
                    File resDir = Files.createDirectory(buildDir, "res");
                    File drawableDir = Files.createDirectory(resDir, "drawable");

                    // Load icon associated with Simple Android application
                    String icon;
                    if (project.getIconPath() == null)
                        icon = "ic_launcher.png";
                    else
                        icon = project.getIconPath().substring(project.getIconPath().lastIndexOf(File.separator) + 1);
                    File outputfile = new File(drawableDir, icon);
                    OutputStream icon_out = null;
                    InputStream icon_in = null;
                    try {
                        if (project.getIconPath() == null) {
                            icon_in = context.getAssets().open("ic_launcher.png");
                            Main.state("project.properties 未找到 icon 路径，已启用默认图标");
                        } else
                            icon_in = new FileInputStream(new File(project.getIconPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (icon_in == null)
                        return false;
                    try {
                        icon_out = new FileOutputStream(outputfile);
                        byte[] b = new byte[1024 * 8];
                        int n;
                        while ((n = icon_in.read(b)) != -1)
                            icon_out.write(b, 0, n);
                        icon_in.close();
                        icon_out.flush();
                        icon_out.close();
                    } catch (IOException ioe) {
                        try {
                            icon_in.close();
                            icon_out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        compiler.error(Scanner.NO_POSITION, Error.errWriteError, outputfile.toString());
                        return false;
                    }

                    // Create and pre-process AndroidManifest.xml
                    out.println("________Generating Manifest File");
                    Main.state("写入Androidmanifest.xml");

                    if (!writeAndroidManifest(compiler, project, buildDir, compiler.generatePermissions())) {
                        Main.err("写入Androidmanifest.xml 失败");
                        return false;
                    }

                    // Create class files
                    Main.state("正在生成class字节码");
                    File classesDir = Files.createDirectory(buildDir, "classes");
                    compiler.generate(classesDir);
                    Main.state("生成class字节码成功!");

                    Main.state("正在生成classes.dex...");
                    // Invoke dx on class files
                    final String dexedClasses = "classes.dex";
                    try {
                        // Using System.err and System.out on purpose. Don't want to pollute build messages with
                        // tools output
                        DxConsole.out = System.out;
                        DxConsole.err = System.err;
                        Arguments args = new Arguments();
                        args.outName = tmpDir.getAbsolutePath() + File.separator + dexedClasses;
                        args.jarOutput = false;
                        args.localInfo = true;
                        args.positionInfo = PositionList.LINES;
                        args.fileNames = new String[]{
                                classesDir.getAbsolutePath(),
//                                SIMPLE_ANDROID_RUNTIME
                                XInit.XRunTime   //xruntime.jar路径
                        };
                        if (com.android.dx.command.dexer.Main.run(args) != 0) {
                            Main.err("调用dx构建classes.dex失败");
                            internalError();
                            return false;
                        }
                    } catch (Throwable t) {
                        Main.err("调用dx构建classes.dex失败");
                        internalError();
                        return false;
                    }
                    Main.state("生成classes.dex成功");
                    Main.state("正在打包apk...");
                    // Invoke aapt to package everything up
                    out.println("________Packaging " + project.getProjectName());
                    // Need to make sure assets directory exists otherwise aapt will fail.

                    Files.createDirectory(project.getAssetsDirectory());
                    File deployDir = Files.createDirectory(buildDir, "deploy");
                    String tmpPackageName = project.getProjectName() + ".ap_";
                    /***chmod 777 aapt****/
                    String chmodcmdLine[] = {"chmod", "777", XInit.AAPT};
                    if (Execution.execute(null, chmodcmdLine, System.out, System.err)) {
                        Utils.log("chmod successful!");
                    }
                    /***chmod 777 aapt****/
                    String aaptPackageCommandline[] = {
                            XInit.AAPT,
                            "package",
                            "-v",
                            "-f",
                            "-M", buildDir.getAbsolutePath() + File.separator + "AndroidManifest.xml",
                            "-S", resDir.getAbsolutePath(),
                            "-A", project.getAssetsDirectory().toString(),
                            "-I", XInit.ANDROID_JAR,
                            "-F", deployDir.getAbsolutePath() + File.separatorChar + tmpPackageName
                    };
                    // Using System.err and System.out on purpose. Don't want to pollute build messages with
                    // tools output
                    if (!Execution.execute(null, aaptPackageCommandline, System.out, System.err)) {
                        Main.err("调用aapt打包失败");
                        internalError();
                        return false;
                    }
                    // Finish and sign apk file
                    String packageName = project.getProjectName() + ".apk";
                    //未签名apk路径
                    String apkAbsolutePath = deployDir.getAbsolutePath() + File.separatorChar + packageName;
                    String apkBuilderCommandline[] = {
                            apkAbsolutePath,
                            "-v",
                            "-u",
                            "-z", deployDir.getAbsolutePath() + File.separatorChar + tmpPackageName,
                            "-f", tmpDir.getAbsolutePath() + File.separator + dexedClasses
                    };

//                    ApkBuilder.main(apkBuilderCommandline);
                    ApkBuilderMain.main(apkBuilderCommandline);
                    Main.state("正在签名...");
//                    签名
                    File apkin = new File(apkAbsolutePath);
                    if (!apkin.exists()) {
                        Main.err("签名失败 未找到apk文件");
                        return false;
                    }
                    File apkout = new File(apkin.getParent() + File.separator + project.getProjectName() + "_signed.apk");
                    try {
                        apksigner.Main.sign(apkin, apkout.getPath());
                    } catch (Exception e) {
                        Utils.log("签名失败");
                        Main.err("签名失败");
                        e.printStackTrace();
                        Main.err("签名失败");
                        return false;
                    }
                    Utils.log("签名成功");
                    apkin.delete();
                    new File(deployDir.getAbsolutePath() + File.separator + tmpPackageName).delete();
                    outpath = apkout.getAbsolutePath();
                    /*******************/
                    //测试终止
//                    if (1 == 1)
//                        return true;

                    // Sign the apk file: if the project file lists a keystore, build a release version of the
                    // application. Otherwise generate a debug keystore, if necessary, and use it to sign the
                    // application
                   /* String jarsignerCommandline[];
                    String keyLocation = project.getKeystoreLocation();

                    if (keyLocation != null && !keyLocation.isEmpty()) {
                        String releaseJarsignerCommandline[] = {
                                JARSIGNER_BINARY,
                                "-keystore", keyLocation,
                                apkAbsolutePath,
                                project.getKeystoreAlias()
                        };
                        jarsignerCommandline = releaseJarsignerCommandline;

                    } else {
                        File keystore = new File(buildDir, ANDROID_DEBUG_KEYSTORE);
                        if (!keystore.exists()) {
                            // Generate debug keystore
                            String keytoolCommandline[] = {
                                    KEYTOOL_BINARY,
                                    "-genkey",
                                    "-keystore", keystore.getAbsolutePath(),
                                    "-alias", "AndroidDebugKey",
                                    "-keyalg", "RSA",
                                    "-storepass", "android",
                                    "-keypass", "android",
                                    "-dname", "CN=Android Debug, O=Android, C=US",
                                    "-validity", "365"
                            };

                            if (!Execution.execute(null, keytoolCommandline, System.out, System.err)) {
                                internalError();
                                return false;
                            }
                        }

                        String debugJarsignerCommandline[] = {
                                JARSIGNER_BINARY,
                                "-keystore", keystore.getAbsolutePath(),
                                "-storepass", "android",
                                apkAbsolutePath,
                                "AndroidDebugKey"
                        };
                        jarsignerCommandline = debugJarsignerCommandline;
                    }

                    if (!Execution.execute(null, jarsignerCommandline, System.out, System.err)) {
                        internalError();
                        return false;
                    }*/
                    break;
            }
        }

        out.println("Build finished in " +
                ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
        Main.state("编译用时: " +
                ((System.currentTimeMillis() - start) / 1000.0) + " 秒");
        if (compiler.errorCount == 0) {
            Main.state("签名成功 生成apk路径: " + outpath);
            Main.success(outpath);
        } else {
            Main.err("编译失败~");
        }
        return compiler.errorCount == 0;
    }

    /**
     * Builds a Simple project for unit test execution.
     *
     * @param project project to build
     * @return list of compiled classfiles or {@code null} if there were any
     * errors
     */
//    public static List<ClassFile> compileForUnitTesting(Project project) {
//        // Create a new compiler instance for the compilation
//        long start = System.currentTimeMillis();
//        final Compiler compiler = new Compiler(Platform.Android, System.out, System.err);
//
//        // Parse all source files
//        for (Project.SourceDescriptor arg : project.getSources()) {
//            final File file = arg.getFile();
//            System.out.println("________Compiling " + file.getPath());
//
//            try {
//                new Parser(compiler, new Scanner(compiler, compiler.createFileIndex(file.getAbsolutePath()),
//                        Files.read(file, compiler.encoding)), arg.getQualifiedName()).parse();
//            } catch (IOException ioe) {
//                compiler.error(Scanner.NO_POSITION, Error.errReadError, arg.getFile().toString());
//            }
//        }
//
//        // Resolve symbol information in parse trees
//        compiler.resolve();
//
//        // Create and package class files if there were no compilation errors
//        List<ClassFile> classes = null;
//        if (compiler.errorCount > 0) {
//            compiler.out.println("Compilation errors: " + compiler.errorCount);
//            Main.err("编译错误个数: " + compiler.errorCount);
//        } else {
//            // Create class files
//            classes = new ArrayList<ClassFile>();
//            compiler.generate(classes);
//        }
//
//        System.out.println("Build finished in " +
//                ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
//
//        return classes;
//    }

    /**
     * Creates a new Simple compiler.
     *
     * @param platform platform to compile for
     * @param out      stdout stream for compiler messages
     * @param err      stderr stream for compiler messages
     */
    public Compiler(Platform platform, PrintStream out, PrintStream err, Context context) {
        this.out = out;
        this.err = err;
        //编码
//        encoding = "Cp1252";
        encoding = "UTF-8";
        objects = new ArrayList<ObjectSymbol>();
        classfiles = new ArrayList<String>();
        globalNamespaceSymbol = new NamespaceSymbol();
        symbolToClassNeedingPermissionsMap = new HashMap<ObjectSymbol, Class<?>>();
        classesNeedingPermissions = new HashSet<Class<?>>();

        // Load Simple runtime library symbol information
        this.platform = platform;
        if (platform != Platform.None) {
            // TODO: support for third party Simple libraries
//            runtimeLoader = new RuntimeLoader(this, ANDROID_RUNTIME, platform.getRuntimeLibrary());
            runtimeLoader = new RuntimeLoader(this, platform.getRuntimeLibrary(), context);
            runtimeLoader.loadSimpleObjects();
        }

        filenameMap = new ArrayList<String>();

        runtimeErrorType = (ObjectType) ObjectSymbol.getObjectSymbol(this,
                RUNTIME_ERROR_INTERNAL_NAME).getType();
    }

    /**
     * Returns the path name of the file associated with the file index
     *
     * @param fileIndex index of file to lookup
     * @return file name of file being looked up
     */
    public String fileIndexToPath(int fileIndex) {
        return fileIndex == 0 ? "" : filenameMap.get(fileIndex - 1);
    }

    /**
     * Returns a new file index for a file.
     *
     * @param path file path
     * @return file index
     */
    public int createFileIndex(String path) {
        filenameMap.add(path);
        return filenameMap.size();
    }

    /**
     * Adds a new Simple object to the list compiled objects.
     *
     * @param objectSymbol object being compiled
     */
    public void addObject(ObjectSymbol objectSymbol) {
        objects.add(objectSymbol);
    }

    /**
     * Adds the given class to the list of generated classes.
     *
     * @param internalName generated class
     */
    public void addClassfile(String internalName) {
        classfiles.add(internalName + ".class");
    }

    /**
     * Returns the internal name of the implementation for the given component
     * object type.
     *
     * @param type component object type
     * @return internal name of component object
     */
    public String getComponentImplementationInternalName(ObjectType type) {
        return runtimeLoader.getComponentImplementationInternalName(type);
    }

    /**
     * Reports a compilation error message.
     *
     * @param position source code position
     * @param error    error message template (see {@link Error})
     * @param params   parameters for error message template
     */
    public void error(long position, String error, String... params) {
        errorCount++;
        new Error(position, error, params).print(this, err);
    }

    /**
     * Reports a compilation warning message.
     *
     * @param warning warning message
     */
    public void warning(Warning warning) {
        warningCount++;
        warning.print(this, out);
    }

    /**
     * Returns the number compilation errors.
     *
     * @return compilation error count
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Returns the number compilation warnings.
     *
     * @return compilation warning count
     */
    public int getWarningCount() {
        return warningCount;
    }

    /**
     * Returns the symbol for the global (unnamed) namespace.
     *
     * @return global namespace symbol
     */
    public NamespaceSymbol getGlobalNamespaceSymbol() {
        return globalNamespaceSymbol;
    }

    /**
     * Returns the base object type for runtime errors.
     *
     * @return runtime error type
     */
    public ObjectType getRuntimeErrorType() {
        return runtimeErrorType;
    }

    /**
     * Returns the current compilation target platform.
     *
     * @return target platform
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Triggers the symbol resolution phase of the compilation.
     * <p/>
     * Note: Visible for testing only! Do not call from outside of this class
     * unless for testing purposes.
     */
    public void resolve() {
        // First resolve declarations
        for (ObjectSymbol objectSymbol : objects) {
            objectSymbol.resolve(this, null);
        }

        // Now that all declarations have been resolved, we attempt to resolve function bodies as well.
        // Doing this in two steps has the advantage that we don't have to worry about cycles in
        // the resolution.
        for (ObjectSymbol objectSymbol : objects) {
            objectSymbol.resolveFunctionBodies(this);
        }
    }

    /*
     * Triggers class file generation.
     */
    private void generate(File buildDirectory) {
        for (int i = 0; i < objects.size(); i++) {
            Main.state("正在生成class字节码 " + (i + 1) + "/" + objects.size());
            ObjectSymbol obj = objects.get(i);
            obj.generate(this, buildDirectory);
        }
//        for (ObjectSymbol objectSymbol : objects) {
//            objectSymbol.generate(this, buildDirectory);
//        }
    }

    /*
     * Triggers in-memory class file generation. Used for unit testing.
     */
    private void generate(List<ClassFile> classes) {
        for (ObjectSymbol objectSymbol : objects) {
            objectSymbol.generate(this, classes);
        }
    }
}
