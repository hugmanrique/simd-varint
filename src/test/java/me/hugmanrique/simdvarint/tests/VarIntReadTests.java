/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.hugmanrique.simdvarint.VarInts;
import org.junit.jupiter.api.Test;

class VarIntReadTests {

  // TODO Test empty array case

  @Test
  void testReadSize1() {
    final byte[] buf = new byte[] { 0x6A };
    assertEquals(106, VarInts.read(buf, 0));
  }

  @Test
  void testReadSize2() {
    final byte[] buf = new byte[] { (byte) 0xFF, 0x01 };
    assertEquals(255, VarInts.read(buf, 0));
  }

  @Test
  void testReadSize5() {
    final byte[] buf = new byte[] { (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x08 };
    assertEquals(-2147483648, VarInts.read(buf, 0));
  }

  @Test
  void testALotOfValues() {
    for (int value = 1; value < (1 << 30); value *= 33) {
      final byte[] buf = new byte[8];
      VarIntTestHelper.write(buf, 0, value);
      assertEquals(value, VarInts.read(buf, 0));
    }
  }

  static final class VarIntTestHelper {

    private static final int MSB = 0x80;
    private static final int DROP_MSB = 0x7F;
    private static final int MAX_BLOCK_VALUE = DROP_MSB;

    // Adapted from https://github.com/dermesser/integer-encoding-rs by L. Bormann and contributors

    static void write(final byte[] dest, int offset, int value) {
      while ((value & ~MAX_BLOCK_VALUE) != 0) {
        dest[offset++] = (byte) (MSB | value);
        value >>>= 7;
      }
      // termination block
      dest[offset] = (byte) value;
    }
  }
}
