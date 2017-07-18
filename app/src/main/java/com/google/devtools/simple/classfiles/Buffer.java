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

/**
 * Automatically resizing byte buffer.
 * 
 * <p>Values bigger than bytes (such as shorts, ints, longs, etc.) are written
 * in big-endian format.
 * 
 * <p>One of the main clients of this class is the bytecode generator which
 * explains the seemingly weird method signatures of the {@code generate()}
 * methods.
 *
 * @author Herbert Czymontek
 */
final class Buffer {

  // Initial buffer size
  private static final int INIT_BUFFER_SIZE = 1024;

  // Size of incremenatal buffer size increases
  private static final int INC_BUFFER_SIZE = 1024;
  
  // The actual data buffer
  private byte[] buffer;

  // The position for the next write operation
  private int offset;

  /**
   * Creates a new instance of Buffer.
   */
  public Buffer() {
    buffer = new byte[INIT_BUFFER_SIZE];
    offset = 0;
  }

  /*
   * Expands the data buffer by the given size.
   */
  private void expandBuffer(int size) {
    byte[] newBuffer = new byte[buffer.length + size];
    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
    buffer = newBuffer;
  }

  /**
   * Writes a byte value into the buffer.
   * 
   * @param b  byte value
   */
  protected void generate8(byte b) {
    try {
      buffer[offset] = b;
      offset += 1;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8(b);
    }
  }

  /**
   * Writes 2 bytes into the buffer.
   * 
   * @param b1  first byte value
   * @param b2  second byte value
   */
  protected void generate8_8(byte b1, byte b2) {
    try {
      buffer[offset + 1] = b2;
      buffer[offset    ] = b1;
      offset += 2;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_8(b1, b2);
    }
  }

  /**
   * Writes 3 bytes into the buffer.
   * 
   * @param b1  first byte value
   * @param b2  second byte value
   * @param b3  third byte value
   */
  protected void generate8_8_8(byte b1, byte b2, byte b3) {
    try {
      buffer[offset + 2] = b3;
      buffer[offset + 1] = b2;
      buffer[offset    ] = b1;
      offset += 3;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_8_8(b1, b2, b3);
    }
  }

  /**
   * Writes a byte followed by short value into the buffer.
   * 
   * @param b  byte value
   * @param s  short value
   */
  protected void generate8_16(byte b, short s) {
    try {
      buffer[offset + 2] = (byte) ( s       & 0xFF);
      buffer[offset + 1] = (byte) ((s >> 8) & 0xFF);
      buffer[offset    ] = b;
      offset += 3;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_16(b, s);
    }
  }

  /**
   * Writes a byte followed by short value and another byte into the buffer.
   * 
   * @param b1  first byte value
   * @param s  short value
   * @param b2  second byte value
   */
  protected void generate8_16_8(byte b1, short s, byte b2) {
    try {
      buffer[offset + 3] = b2;
      buffer[offset + 2] = (byte) ( s       & 0xFF);
      buffer[offset + 1] = (byte) ((s >> 8) & 0xFF);
      buffer[offset    ] = b1;
      offset += 4;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_16_8(b1, s, b2);
    }
  }

  /**
   * Writes a byte followed by short value and two more bytes into the buffer.
   * 
   * @param b1  first byte value
   * @param s  short value
   * @param b2  second byte value
   * @param b3  third byte value
   */
  protected void generate8_16_8_8(byte b1, short s, byte b2, byte b3) {
    try {
      buffer[offset + 4] = b3;
      buffer[offset + 3] = b2;
      buffer[offset + 2] = (byte) ( s       & 0xFF);
      buffer[offset + 1] = (byte) ((s >> 8) & 0xFF);
      buffer[offset    ] = b1;
      offset += 5;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_16_8_8(b1, s, b2, b3);
    }
  }

  /**
   * Writes 2 bytes followed by a short into the buffer.
   * 
   * @param b1  first byte value
   * @param b2  second byte value
   * @param s  short value
   */
  protected void generate8_8_16(byte b1, byte b2, short s) {
    try {
      buffer[offset + 3] = (byte) ( s       & 0xFF);
      buffer[offset + 2] = (byte) ((s >> 8) & 0xFF);
      buffer[offset + 1] = b2;
      buffer[offset    ] = b1;
      offset += 4;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_8_16(b1, b2, s);
    }
  }

  /**
   * Writes 2 bytes followed by a short and another byte into the byte buffer.
   * 
   * @param b1  first byte value
   * @param b2  second byte value
   * @param s  short value
   * @param b3  third byte value
   */
  protected void generate8_8_16_8(byte b1, byte b2, short s, byte b3) {
    try {
      buffer[offset + 4] = b3;
      buffer[offset + 3] = (byte) ( s       & 0xFF);
      buffer[offset + 2] = (byte) ((s >> 8) & 0xFF);
      buffer[offset + 1] = b2;
      buffer[offset    ] = b1;
      offset += 5;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate8_8_16_8(b1, b2, s, b3);
    }
  }

  /**
   * Writes a short value into the byte buffer.
   * 
   * @param s  short value
   */
  protected void generate16(short s) {
    try {
      buffer[offset + 1] = (byte) ( s       & 0xFF);
      buffer[offset    ] = (byte) ((s >> 8) & 0xFF);
      offset += 2;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate16(s);
    }
  }

  /**
   * Writes two short values into the byte buffer.
   * 
   * @param s1  first short value
   * @param s2  second short value
   */
  protected void generate16_16(short s1, short s2) {
    try {
      buffer[offset + 3] = (byte) ( s2       & 0xFF);
      buffer[offset + 2] = (byte) ((s2 >> 8) & 0xFF);
      buffer[offset + 1] = (byte) ( s1       & 0xFF);
      buffer[offset    ] = (byte) ((s1 >> 8) & 0xFF);
      offset += 4;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate16_16(s1, s2);
    }
  }

  /**
   * Writes an int value into the byte buffer.
   * 
   * @param i  int value
   */
  protected void generate32(int i) {
    try {
      buffer[offset + 3] = (byte) ( i        & 0xFF);
      buffer[offset + 2] = (byte) ((i >>  8) & 0xFF);
      buffer[offset + 1] = (byte) ((i >> 16) & 0xFF);
      buffer[offset    ] = (byte) ((i >> 24) & 0xFF);
      offset += 4;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate32(i);
    }
  }

  /**
   * Writes an long value into the byte buffer.
   * 
   * @param l  long value
   */
  protected void generate64(long l) {
    try {
      buffer[offset + 7] = (byte)( l        & 0xFF);
      buffer[offset + 6] = (byte)((l >>  8) & 0xFF);
      buffer[offset + 5] = (byte)((l >> 16) & 0xFF);
      buffer[offset + 4] = (byte)((l >> 24) & 0xFF);
      buffer[offset + 3] = (byte)((l >> 32) & 0xFF);
      buffer[offset + 2] = (byte)((l >> 40) & 0xFF);
      buffer[offset + 1] = (byte)((l >> 48) & 0xFF);
      buffer[offset    ] = (byte)((l >> 56) & 0xFF);
      offset += 8;
    } catch (ArrayIndexOutOfBoundsException e) {
      expandBuffer(INC_BUFFER_SIZE);
      generate64(l);
    }
  }

  /**
   * Copies an array of byte into the byte buffer.
   * 
   * @param bytes  bytes to copy
   */
  protected void generateBytes(byte[] bytes) {
    // First need to make sure that the byte buffer is big enough
    if (buffer.length - offset < bytes.length) {
      expandBuffer(bytes.length);
    }

    // Then copy the data
    System.arraycopy(bytes, 0, buffer, offset, bytes.length);
    offset += bytes.length;
  }

  /**
   * Changes the value of a byte at the given position within the byte buffer.
   * 
   * @param off  position of byte to change
   * @param b  new byte value
   */
  protected void patch8(int off, byte b) {
    buffer[off] = b;
  }

  /**
   * Changes the value of a short at the given position within the byte buffer.
   * 
   * @param off  position of byte to change
   * @param s  new short value
   */
  protected void patch16(int off, short s) {
    buffer[off + 1] = (byte) ( s       & 0xFF);
    buffer[off    ] = (byte) ((s >> 8) & 0xFF);
  }

  /**
   * Returns the position of the next write operation within the byte buffer.
   * 
   * @return  next write position
   */
  protected int getOffset() {
    return offset;
  }

  /**
   * Returns a byte array containing the byte data from the buffer.
   * 
   * @return  byte buffer data
   */
  protected byte[] toByteArray() {
    byte[] buf = buffer;
    if (buffer.length != offset) {
      // Shrink the buffer if the built-in byte array is too large
      buf = new byte[offset];
      System.arraycopy(buffer, 0, buf, 0, offset);
    }
    return buf;
  }
}
