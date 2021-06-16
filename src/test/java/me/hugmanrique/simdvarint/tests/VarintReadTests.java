/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import me.hugmanrique.simdvarint.Varints;
import org.junit.jupiter.api.Test;

class VarintReadTests {

  static void assertRead(final byte[] src, final int expected, final int expectedLength) {
    final var buf = ByteBuffer.wrap(src);

    assertEquals(expected, Varints.read(src, 0));
    assertEquals(expected, Varints.read(buf, 0));
    assertEquals(expected, Varints.read(buf));
    assertEquals(expectedLength, buf.position());
  }

  static void assertReadThrows(final byte[] src) {
    final var buf = ByteBuffer.wrap(src);

    assertThrows(IllegalArgumentException.class, () -> Varints.read(src, 0));
    assertThrows(IllegalArgumentException.class, () -> Varints.read(buf, 0));
    assertThrows(IllegalArgumentException.class, () -> Varints.read(buf));
  }

  @Test
  void testEmptyByteArray() {
    assertThrows(IndexOutOfBoundsException.class, () -> Varints.read(new byte[0], 0));
  }

  @Test
  void testEmptyBuffer() {
    final var buf = ByteBuffer.allocate(0);

    assertThrows(IndexOutOfBoundsException.class, () -> Varints.read(buf));
    assertThrows(IndexOutOfBoundsException.class, () -> Varints.read(buf, 0));
  }

  @Test
  void test1Byte() {
    assertRead(new byte[] { 0x00 }, 0, 1);
    assertRead(new byte[] { 0x01 }, 1, 1);
    assertRead(new byte[] { 0x61 }, 97, 1);
    assertRead(new byte[] { 0x6A }, 106, 1);
    assertRead(new byte[] { 0x7F }, (1 << 7) - 1, 1);
  }

  @Test
  void test2Bytes() {
    assertRead(new byte[] { (byte) 0x80, 0x01 }, 1 << 7, 2);
    assertRead(new byte[] { (byte) 0xFF, 0x01 }, 255, 2);
    assertRead(new byte[] { (byte) 0xD9, 0x1A }, 3417, 2);
    assertRead(new byte[] { (byte) 0xEF, 0x30 }, 6255, 2);
    assertRead(new byte[] { (byte) 0xFF, 0x7F }, (1 << 14) - 1, 2);
  }

  @Test
  void test3Bytes() {
    assertRead(new byte[] { (byte) 0x80, (byte) 0x80, 0x01 }, 1 << 14, 3);
    assertRead(new byte[] { (byte) 0xB1, (byte) 0xE5, 0x03 }, 62129, 3);
    assertRead(new byte[] { (byte) 0xE5, (byte) 0x8E, 0x26 }, 624485, 3);
    assertRead(new byte[] { (byte) 0xD4, (byte) 0xE6, 0x3A }, 963412, 3);
    assertRead(new byte[] { (byte) 0xFF, (byte) 0xFF, 0x7F }, (1 << 21) - 1, 3);
  }

  @Test
  void test4Bytes() {
    assertRead(new byte[] { (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01 },
        1 << 21, 4);
    assertRead(new byte[] { (byte) 0xA5, (byte) 0x9C, (byte) 0xB5, 0x01 },
        2969125, 4);
    assertRead(new byte[] { (byte) 0xFA, (byte) 0xCD, (byte) 0xB9, 0x12 },
        38692602, 4);
    assertRead(new byte[] { (byte) 0xDA, (byte) 0xDC, (byte) 0xCA, 0x19 },
        53653082, 4);
    assertRead(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x7F },
        (1 << 28) - 1, 4);
  }

  @Test
  void test5Bytes() {
    assertRead(new byte[] { (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01 },
        1 << 28, 5);
    assertRead(new byte[] { (byte) 0x8E, (byte) 0x98, (byte) 0xC0, (byte) 0xBF, 0x02 },
        670043150, 5);
    assertRead(new byte[] { (byte) 0x8A, (byte) 0xDD, (byte) 0xED, (byte) 0xFD, 0x04 },
        1337683594, 5);
    assertRead(new byte[] { (byte) 0xBD, (byte) 0xB9, (byte) 0x9C, (byte) 0xB6, 0x05 },
        1455889597, 5);
    assertRead(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x07 },
        Integer.MAX_VALUE, 5);

    assertRead(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0F },
        -1, 5);
    assertRead(new byte[] { (byte) 0xEF, (byte) 0xD1, (byte) 0xFF, (byte) 0xFF, 0x0F },
        -5905, 5);
    assertRead(new byte[] { (byte) 0xAC, (byte) 0xEC, (byte) 0xFD, (byte) 0xFF, 0x0F },
        -35284, 5);
    assertRead(new byte[] { (byte) 0xB3, (byte) 0xA2, (byte) 0xB3, (byte) 0xFD, 0x0F },
        -5451469, 5);
    assertRead(new byte[] { (byte) 0xC4, (byte) 0xF7, (byte) 0x9F, (byte) 0xFE, 0x0E },
        -272106556, 5);
    assertRead(new byte[] { (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x08 },
        Integer.MIN_VALUE, 5);
  }

  @Test
  void testThrowsIfTooBig() {
    assertReadThrows(new byte[] {
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01 });
    assertReadThrows(new byte[] {
        (byte) 0x82, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xA0, 0x01 });
  }

  @Test
  void testMasksSuccessive() {
    assertRead(new byte[] { 0x03, (byte) 0x98, (byte) 0xDA, 0x1C }, 3, 1);
    assertRead(new byte[] { (byte) 0xD2, 0x09, (byte) 0xAE, 0x2C }, 1234, 2);
    assertRead(new byte[] {
        (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x08,
        (byte) 0x8C, (byte) 0xF2, (byte) 0xA1, (byte) 0x8F, 0x07 }, Integer.MIN_VALUE, 5);
  }
}
