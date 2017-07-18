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

package com.google.devtools.simple.runtime;

import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.errors.FileAlreadyExistsError;
import com.google.devtools.simple.runtime.errors.FileIOError;
import com.google.devtools.simple.runtime.errors.NoSuchFileError;
import com.google.devtools.simple.runtime.errors.UnknownFileHandleError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of various file related runtime functions.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public final class Files {

  /**
   * Provides access to the root directory of the application.
   */
  public static interface RootProvider {
    File getApplicationRootDirectory();
  }
  
  /*
   * Helper class for handling file I/O.
   */
  private static class FileDescriptor {
    private final File file;
    private RandomAccessFile raf;

    private void raiseError(String message) {
      throw new FileIOError(message + ": " + file.getName());
    }

    FileDescriptor(File file) {
      this.file = file;
      try {
        raf = new RandomAccessFile(file, "rws");
      } catch (FileNotFoundException e) {
        raiseError("Opening file");
      }
    }

    void close() {
        try {
          raf.close();
        } catch (IOException e) {
          raiseError("Closing file");
        }
    }
  }

  /*
   * Simple is a single threaded language. Therefore no further
   * synchronization is needed here.
   */
  private static int fileHandleCounter;
  private static Map<Integer, FileDescriptor> fileHandleMap =
      new HashMap<Integer, FileDescriptor>();

  private static File rootDirectory;

  private Files() {  // COV_NF_LINE
  }                  // COV_NF_LINE

  /**
   * Sets a root directory for file access by the application.
   *
   * @param rootDir  root directory for file access by the application
   */
  public static void initialize(File rootDir) {
    rootDirectory = rootDir;
  }
  
  /*
   * Gets a File object for a given file name and raises a runtime error if the
   * file doesn't exist.
   */
  private static File getExistingFile(String name) {
    File file = new File(rootDirectory, name);
    if (!file.exists()) {
      throw new NoSuchFileError(name);
    }
    return file;
  }

  /*
   * Gets a File object for a given file name and raises a runtime error if the
   * file already exists.
   */
  private static File getNonExistingFile(String name) {
    File file = new File(rootDirectory, name);
    if (file.exists()) {
      throw new FileAlreadyExistsError(name);
    }
    return file;
  }

  /*
   * Gets the file descriptor associated with the file handle. Raises a runtime
   * error if no file descriptor can be found.
   */
  private static FileDescriptor getFileDescriptor(int handle) {
    FileDescriptor descriptor = fileHandleMap.get(handle);
    if (descriptor == null) {
      throw new UnknownFileHandleError();
    }
    return descriptor;
  }

  /*
   * Raises a file write runtime error.
   */
  private static void writeError(String name) {
    throw new FileIOError("Write error: " + name);
  }

  /*
   * Raises a file read runtime error.
   */
  private static void readError(String name) {
    throw new FileIOError("Read error: " + name);
  }

  /*
   * Raises a file read runtime error.
   */
  private static void seekError(String name) {
    throw new FileIOError("Seek error: " + name);
  }

  /*
   * Raises a file access runtime error.
   */
  private static void accessError(String name) {
    throw new FileIOError("Access error: " + name);
  }
  
  /**
   * Renames a file. Causes a runtime error if the file doesn't exist. 
   * 
   * @param oldname  file name before renaming
   * @param newname  file name after renaming
   */
  @SimpleFunction
  public static void Rename(String oldname, String newname) {

    File oldfile = getExistingFile(oldname);
    File newfile = new File(rootDirectory, newname);

    if (!oldfile.equals(newfile)) {
      if (newfile.exists()) {
        throw new FileAlreadyExistsError(newname);
      }
  
      if (!oldfile.renameTo(newfile)) {
        accessError(oldname);
      }
    }
  }

  /**
   * Deletes a file.
   * 
   * @param name  name of file to delete
   */
  @SimpleFunction
  public static void Delete(String name) {
    File file = getExistingFile(name);
    if (file.isDirectory() || !file.delete()) {
      accessError(name);
    }
  }
  
  /**
   * Creates a new directory.
   * 
   * @param name  name of new directory
   */
  @SimpleFunction
  public static void Mkdir(String name) {
    if (!getNonExistingFile(name).mkdir()) {
      accessError(name);
    }
  }

  /**
   * Deletes a directory.
   * 
   * @param name  name of directory to delete
   */
  @SimpleFunction
  public static void Rmdir(String name) {
    File directory = getExistingFile(name);
    if (!directory.isDirectory() || !directory.delete()) {
      accessError(name);
    }
  }

  /**
   * Checks whether the given name is the name of an existing directory.
   * Causes a runtime error if the directory doesn't exist.
   * 
   * @param name  name to check
   * @return  {@code true} if the name belongs to an existing directory,
   *          {@code false} otherwise
   */
  @SimpleFunction
  public static boolean IsDirectory(String name) {
    return getExistingFile(name).isDirectory();
  }

  /**
   * Checks whether a file or directory exists.
   * 
   * @param name  file to check
   * @return  {@code true} if the file or directory exists, {@code false}
   *          otherwise
   */
  @SimpleFunction
  public static boolean Exists(String name) {
    return new File(rootDirectory, name).exists();
  }

  /**
   * Opens an existing file or creates a new file for reading or writing. 
   * 
   * @param name  name of file to open or create
   * @return  file handle
   */
  @SimpleFunction
  public static int Open(String name) {
    File file = new File(rootDirectory, name);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        Log.Error(Log.MODULE_NAME_RTL, e.getMessage());
        e.printStackTrace();

        throw new FileIOError("Creating file: " + name);
      }
    } else if (IsDirectory(name)) {
      throw new FileIOError("Cannot open directory: " + name);
    }
    
    FileDescriptor descriptor = new FileDescriptor(file);
    int handle = ++fileHandleCounter;

    fileHandleMap.put(handle, descriptor);

    return handle;
  }

  /**
   * Closes a file previously opened.
   * 
   * @param handle  handle of file to close
   */
  @SimpleFunction
  public static void Close(int handle) {
    getFileDescriptor(handle).close();
    fileHandleMap.remove(handle);
  }

  /**
   * Checks whether the current file position is at the end of the file.
   *
   * @param handle  handle of file to check
   * @return  {@code true} if the end of the file was reaches, {@code false}
   *          otherwise
   */
  @SimpleFunction
  public static boolean Eof(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.getFilePointer() == descriptor.file.length();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return false;  // COV_NF_LINE
    }
  }

  /**
   * Positions the file pointer to an absolute position.
   * 
   * @param handle  handle of file
   * @param offset  absolute position within file
   * @return  new position within file
   */
  @SimpleFunction
  public static long Seek(int handle, long offset) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      if (offset < 0 || offset > descriptor.file.length()) {
        seekError(descriptor.file.getName());
      }
      descriptor.raf.seek(offset);
      long newOffset = descriptor.raf.getFilePointer();
      if (newOffset != offset) {
        seekError(descriptor.file.getName());
      }
      return newOffset;
    } catch (IOException e) {
      seekError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }

  /**
   * Returns the size of a file.
   * 
   * @param handle  handle of file
   * @return  file size
   */
  @SimpleFunction
  public static long Size(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    return descriptor.file.length();
  }

  /**
   * Writes a String to a file. 
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteString(int handle, String value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeUTF(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a String value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static String ReadString(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readUTF();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return "";  // COV_NF_LINE
    }
  }

  /**
   * Writes a Boolean value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteBoolean(int handle, boolean value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeBoolean(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a Boolean value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static boolean ReadBoolean(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readBoolean();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return false;  // COV_NF_LINE
    }
  }


  /**
   * Writes a Byte value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteByte(int handle, byte value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeByte(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a Byte value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static byte ReadByte(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readByte();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }

  /**
   * Writes a Short value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteShort(int handle, short value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeShort(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a Short value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static short ReadShort(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readShort();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }

  /**
   * Writes an Integer boolean value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteInteger(int handle, int value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeInt(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads an Integer value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static int ReadInteger(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readInt();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }

  /**
   * Writes a Long value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteLong(int handle, long value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeLong(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a Long value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static long ReadLong(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readLong();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }

  /**
   * Writes a Single value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteSingle(int handle, float value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeFloat(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a Single value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static float ReadSingle(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readFloat();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }

  /**
   * Writes a Double value to a file.
   * 
   * @param handle  handle of file
   * @param value  value to write
   */
  @SimpleFunction
  public static void WriteDouble(int handle, double value) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      descriptor.raf.writeDouble(value);
    } catch (IOException e) {
      writeError(descriptor.file.getName());
    }
  }

  /**
   * Reads a Double value from a file.
   * 
   * @param handle  handle of file
   * @return  value read
   */
  @SimpleFunction
  public static double ReadDouble(int handle) {
    FileDescriptor descriptor = getFileDescriptor(handle);
    try {
      return descriptor.raf.readDouble();
    } catch (IOException e) {
      readError(descriptor.file.getName());
      return 0;  // COV_NF_LINE
    }
  }
}
