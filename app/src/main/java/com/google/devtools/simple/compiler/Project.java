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

import com.google.devtools.simple.util.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 解决中文乱码
 * String value = "中国";
 * String value2 = new String(value.getBytes(),"ISO-8859-1");
 * System.out.println(p.getProperty(value2));
 * This class gives access to Simple project files.
 * <p/>
 * <p>A Simple project file is essentially a Java properties file.
 *
 * @author Herbert Czymontek
 */
public final class Project {

    /**
     * Representation of a source file containing its name and file location.
     */
    public static class SourceDescriptor {

        // Qualified name of the class defined by the source file
        private final String qualifiedName;

        // File descriptor for the source
        private final File file;

        private SourceDescriptor(String qualifiedName, File file) {
            this.qualifiedName = qualifiedName;
            this.file = file;
        }

        /**
         * Returns the qualified name of the class defined by the source file.
         *
         * @return class name of source file
         */
        public String getQualifiedName() {
            return qualifiedName;
        }

        /**
         * Returns a file descriptor for the source file
         *
         * @return file descriptor
         */
        public File getFile() {
            return file;
        }
    }

    /*
     * Property tags defined in the project file:
     *
     *    main - qualified name of main form class
     *    name - application name
     *    source - comma separated list of source root directories
     *    assets - assets directory (for image and data files bundled with the application)
     *    build - output directory for the compiler
     *    key - location of key store for signing Android applications
     */
    private static final String MAINTAG = "main";
    private static final String NAMETAG = "name";
    private static final String SOURCETAG = "source";
    private static final String ASSETSTAG = "assets";
    private static final String BUILDTAG = "build";
    private static final String KEYLOCATIONTAG = "key.location";
    private static final String KEYALIASTAG = "key.alias";
    //添加
    private static final String VERSIONCODETAG = "versioncode";
    private static final String VERSIONNAMETAG = "versionname";
    //图标路径
    private static final String ICONTAG = "icon";
    //包名
    private static final String PACKAGENAMETAG = "packagename";
    //创建时间
    private static final String CREATEDATETAG = "createdate";

    // Simple source file extension扩展名
    public static final String SOURCEFILE_EXTENSION = ".x";

    // Table containing project properties
    private Properties properties;

    // Project root directory
    private String projectRoot;

    // Build output directory override, or null.
    private String buildDirOverride;

    // List of source files
    private List<SourceDescriptor> sources;

    /**
     * Creates a new Simple project descriptor.
     *
     * @param projectFile path to project file
     * @throws IOException if the project file cannot be read
     */
    public Project(String projectFile) throws IOException {
        this(new File(projectFile));
    }

    /**
     * Creates a new Simple project descriptor.
     *
     * @param projectFile      path to project file     project.properties
     * @param buildDirOverride build output directory override, or null
     * @throws IOException if the project file cannot be read
     */
    public Project(String projectFile, String buildDirOverride) throws IOException {
        this(new File(projectFile));
        this.buildDirOverride = buildDirOverride;
    }

    /**
     * Creates a new Simple project descriptor.
     *
     * @param file project file
     * @throws IOException if the project file cannot be read
     */
    public Project(File file) throws IOException {
        File parentFile = Preconditions.checkNotNull(file.getParentFile());
        //项目根目录
        projectRoot = parentFile.getAbsolutePath();

        // Load project file
        properties = new Properties();
        FileInputStream in = new FileInputStream(file);
        try {
            properties.load(in);
        } finally {
            in.close();
        }
    }

    /***
     * 添加
     ***/
    /**
     * 解决中文乱码
     * ISO-8859-1转UTF-8
     *
     * @param s
     * @return
     */
    private String forChinese(String s) {
        if (s == null || s.equals("")) {
            return "";
        }
        try {
            return new String(s.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Main.state(s + " ISO-8859-1转UTF-8错误");
        return "";
    }

    public String getCreateDate() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        return properties.getProperty(CREATEDATETAG) == null ? sf.format(new Date()) : forChinese(properties.getProperty(CREATEDATETAG));
    }

    /**
     * 获取包名
     *
     * @return
     */
    public String getXPackagename() {
        return properties.getProperty(PACKAGENAMETAG) == null ? "" : properties.getProperty(PACKAGENAMETAG);
    }

    public String getIconPath() {
        if (properties.getProperty(ICONTAG) == null) {
            return "";
        }
        String path = projectRoot + File.separator + forChinese(properties.getProperty(ICONTAG));
//        Utils.log("iconpath="+path);
        if (new File(path).exists() && new File(path).isFile())
            return path;
        else return null;
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public String getVersionName() {
        String vn = properties.getProperty(VERSIONNAMETAG) == null ? "1.0" : properties.getProperty(VERSIONNAMETAG);
        vn = forChinese(vn);
        return vn;
    }

    /**
     * 获取versioncode
     *
     * @return
     */
    public String getVersionCode() {
        return properties.getProperty(VERSIONCODETAG) == null ? "1" : properties.getProperty(VERSIONCODETAG);
    }
    /***添加***/

    /**
     * Returns the name of the main form class
     *
     * @return main form class name
     */
    public String getMainForm() {
        return forChinese(properties.getProperty(MAINTAG));
    }

    /**
     * Sets the name of the main form class.
     *
     * @param main main form class name
     */
    public void setMainForm(String main) {
        properties.setProperty(MAINTAG, main);
    }

    /**
     * Returns the name of the project (application).
     *
     * @return project name
     */
    public String getProjectName() {
        String proname = forChinese(properties.getProperty(NAMETAG));
        if (proname.equals(""))
            return "xruntim";
        else
            return proname;
    }

    /**
     * Sets the name of the project (application)
     *
     * @param name project name
     */
    public void setProjectName(String name) {
        properties.setProperty(NAMETAG, name);
    }

    /**
     * Returns the root path of the project.
     *
     * @return project root path
     */
    public String getProjectRoot() {
        return forChinese(projectRoot);
    }

    /**
     * Returns the location of the assets directory.
     *
     * @return assets directory
     */
    public File getAssetsDirectory() {
        return new File(projectRoot, forChinese(properties.getProperty(ASSETSTAG)));
    }

    /**
     * Returns the location of the build output directory.
     *
     * @return build output directory
     */
    public File getBuildDirectory() {
        if (buildDirOverride != null) {
            return new File(buildDirOverride);
        }
        return new File(projectRoot, forChinese(properties.getProperty(BUILDTAG)));
    }

    /**
     * Returns the location of the keystore for signing Android applications.
     *
     * @return location of keystore
     */
    public String getKeystoreLocation() {
        return forChinese(properties.getProperty(KEYLOCATIONTAG));
    }

    /**
     * Sets the location of the keystore for signing Android applications.
     *
     * @param location location of keystore
     */
    public void setKeystoreLocation(String location) {
        properties.setProperty(KEYLOCATIONTAG, location);
    }

    /**
     * Returns the alias name of the keystore for signing Android applications.
     *
     * @return alias name of keystore
     */
    public String getKeystoreAlias() {
        return forChinese(properties.getProperty(KEYALIASTAG));
    }

    /**
     * Sets the alias name of the keystore for signing Android applications.
     *
     * @param alias alias name of keystore
     */
    public void setKeystoreAlias(String alias) {
        properties.setProperty(KEYALIASTAG, alias);
    }

    /*
     * Recursively visits source directories and adds found Simple source files to the list of source
     * files.
     */
    private void visitSourceDirectories(String root, File file) {
        if (file.isDirectory()) {
            // Recursively visit nested directories.
            for (String child : file.list()) {
                visitSourceDirectories(root, new File(file, child));
            }
        } else {
            // Add Simple source files to the source file list
            if (file.getName().endsWith(SOURCEFILE_EXTENSION)) {
                String absName = file.getAbsolutePath();
                String name = absName.substring(root.length() + 1, absName.length() -
                        SOURCEFILE_EXTENSION.length());
                sources.add(new SourceDescriptor(name.replace(File.separatorChar, '.'), file));
            }
        }
    }

    /**
     * Returns a list of Simple source files in the project.
     *
     * @return list of source files
     */
    public List<SourceDescriptor> getSources() {
        // Lazily discover source files
        if (sources == null) {
            sources = new ArrayList<SourceDescriptor>();
            String sourceTag = forChinese(properties.getProperty(SOURCETAG));
            for (String sourceDir : sourceTag.split(",")) {
//                src目录
                File dir = new File(projectRoot + File.separatorChar + sourceDir);
                visitSourceDirectories(dir.getAbsolutePath(), dir);
            }
        }
        return sources;
    }
}
