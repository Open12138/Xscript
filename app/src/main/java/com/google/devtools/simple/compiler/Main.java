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

import android.content.Context;
import android.os.Handler;

import com.google.devtools.simple.compiler.Compiler.Platform;

import java.io.IOException;

/**
 * Main entry point for the command line version of the Simple compiler.
 *
 * @author Herbert Czymontek
 */
public final class Main {

    // COV_NF_START

    private Main() {
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
   /* public static void main(String[] args) {
//    arg[0]  properties File
        if (args.length != 1) {
            System.err.println("Usage: simplec <projectfile>");
        } else {
            try {
                com.google.devtools.simple.compiler.Compiler.compile(Platform.Android, new Project(args[0], null), System.out, System.err);
            } catch (IOException ioe) {
                System.err.println("Cannot read project file '" + args[0] + "'");
            }
        }
    }*/

    // 编译入口

    public static final int SUCESS = 0;
    public static final int FAILED = 1;
    public static final int STATE = 2;

    public static Handler handler;
    public static StringBuffer sb = new StringBuffer();

    /**
     * @param context
     * @param propertiesfile project.properties
     */
    public static void xCompile(Context context, String propertiesfile, Handler h) {
        sb.delete(0, sb.length());
        handler = h;
        try {
            com.google.devtools.simple.compiler.Compiler.compile(Platform.Android, new Project(propertiesfile, null), System.out, System.err, context);
        } catch (IOException ioe) {
            System.err.println("Cannot read project file '" + propertiesfile + "'");
            err("Cannot read project file '" + propertiesfile + "'");
        }
    }

    public static void err(String err) {
        sb.append(err).append("\n");
        if (handler != null)
            handler.obtainMessage(Main.FAILED, err).sendToTarget();
    }

    public static void success(String str) {
        sb.append(str).append("\n");
        if (handler != null)
            handler.obtainMessage(Main.SUCESS, str).sendToTarget();
    }

    public static void state(String str) {
        sb.append(str).append("\n");
        if (handler != null)
            handler.obtainMessage(Main.STATE, str).sendToTarget();
    }
}
