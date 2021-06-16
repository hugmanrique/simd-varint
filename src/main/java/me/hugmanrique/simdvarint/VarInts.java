/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

/**
 * Provides utilities for reading and writing base-128 varints.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/encoding">Specification</a>
 */
public final class VarInts {

  private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_64;
  private static final VectorSpecies<Integer> INT_SPECIES = IntVector.SPECIES_256; // > 32*MAX_BYTES
  private static final int MAX_BYTES = 5;
  private static final byte DROP_MSB = (byte) 0x7F;

  @SuppressWarnings("unchecked")
  private static final VectorMask<Integer>[] MASKS = new VectorMask[MAX_BYTES];
  private static final IntVector SHIFT_BY;

  static {
    for (int i = 0; i < MAX_BYTES; i++) {
      // Masks are indexed by the index of the last byte of a varint.
      MASKS[i] = INT_SPECIES.indexInRange(0, i + 1);
    }

    final int[] shiftBy = new int[MAX_BYTES];
    for (int i = 0; i < shiftBy.length; i++) {
      shiftBy[i] = 7 * i;
    }
    final VectorMask<Integer> shiftMask = INT_SPECIES.indexInRange(0, MAX_BYTES);
    SHIFT_BY = IntVector.fromArray(INT_SPECIES, shiftBy, 0, shiftMask);
  }

  private VarInts() {
    throw new AssertionError();
  }

  private static int read(final ByteVector raw) {
    // Every byte of a varint, except the last, has the most-significant bit set.
    // A lane value is negative if and only if the MSB is set. For a N-byte value,
    // the lanes in the range [offset..offset + N - 2] are set, the lane at index
    // offset + N - 1 is unset, and the remaining lanes may be set.
    final VectorMask<Byte> contMask = raw.test(VectorOperators.IS_NEGATIVE);
    final int endIndex = contMask.not().firstTrue();
    if (endIndex >= MAX_BYTES) {
      throw new IllegalArgumentException("Found malformed VarInt");
    }

    // Unset the most-significant bit and convert lane values from bytes to ints.
    final IntVector values = (IntVector) raw.and(DROP_MSB)
        .convertShape(VectorOperators.B2I, INT_SPECIES, 0); // contraction

    // Shift the value of each lane 7 * N positions to the left, where N is
    // the lane index. Finally, sum their values, masking the remaining lanes.
    final VectorMask<Integer> varintMask = MASKS[endIndex];
    return values
      .lanewise(VectorOperators.LSHL, SHIFT_BY)
      .reduceLanes(VectorOperators.OR, varintMask);
  }

  /**
   * Reads a base-128 varint from the given array starting at the given offset,
   * according to {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order.
   *
   * @param src    the array to read from
   * @param offset the offset into the array
   * @return the decoded integer
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code offset >= src.length}
   * @throws IllegalArgumentException  if the varint is malformed
   */
  public static int read(final byte[] src, final int offset) {
    final VectorMask<Byte> srcMask = SPECIES.indexInRange(offset, src.length);
    final ByteVector raw = ByteVector.fromArray(SPECIES, src, offset, srcMask);
    return read(raw);
  }

  /**
   * Reads a base-128 varint from the given buffer starting at its current position,
   * according to its {@linkplain ByteBuffer#order() byte order}.
   *
   * @param buffer the buffer to read from
   * @return the decoded integer
   * @throws IllegalArgumentException if the varint is malformed
   */
  public static int read(final ByteBuffer buffer) {
    // TODO Specify if we increment buffer position. We currently don't.
    final ByteVector raw =
        ByteVector.fromByteBuffer(SPECIES, buffer, buffer.position(), buffer.order());
    return read(raw);
  }

  public static void write(final byte[] dest, final int pos, final int value) {
    throw new UnsupportedOperationException();
  }

  public static void write(final ByteBuffer buffer, final int value) {
    throw new UnsupportedOperationException();
  }
}
