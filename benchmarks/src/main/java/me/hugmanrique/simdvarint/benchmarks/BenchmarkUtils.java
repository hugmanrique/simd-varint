/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint.benchmarks;

import java.util.Random;

public final class BenchmarkUtils {

  private static final int MSB = 0x80;
  public static final int MAX_VARINT_BYTES = 5;

  // TODO Replace by Varints method
  public static void writeVarint(final byte[] dest, int offset, int value) {
    while ((value & MSB) != 0) {
      dest[offset++] = (byte) (MSB | value);
      value >>>= 7;
    }

    // termination block
    dest[offset] = (byte) value;
  }

  public static int generateRandomBitNumber(final Random random, final int bitCount) {
    int lowerBound = (1 << (bitCount - 1));
    int upperBound = (1 << bitCount) - 1;
    if (lowerBound == upperBound) {
      return lowerBound;
    }
    return lowerBound + random.nextInt(upperBound - lowerBound);
  }
}
