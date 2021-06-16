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
public final class Varints {

  private static final int MAX_BYTES = 5;
  private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_64;
  private static final VectorSpecies<Integer> INT_SPECIES = IntVector.SPECIES_256; // > 32*MAX_BYTES
  private static final ByteVector DROP_MSB = ByteVector.broadcast(SPECIES, (byte) 0x7F);

  // The N-th mask is a series of N + 1 set lanes followed by a series of unset lanes.
  @SuppressWarnings("unchecked")
  private static final VectorMask<Integer>[] VARINT_MASKS = new VectorMask[MAX_BYTES];

  // When passed as the second operand to a LSHL (logical left shift) operation,
  // shifts the value of each lane 7 * N positions, where N is the lane index.
  private static final IntVector SHIFT_BY;

  static {
    for (int i = 0; i < MAX_BYTES; i++) {
      VARINT_MASKS[i] = INT_SPECIES.indexInRange(0, i + 1);
    }

    final int[] shiftBy = new int[MAX_BYTES];
    for (int i = 0; i < shiftBy.length; i++) {
      shiftBy[i] = 7 * i;
    }
    final VectorMask<Integer> shiftMask = INT_SPECIES.indexInRange(0, MAX_BYTES);
    SHIFT_BY = IntVector.fromArray(INT_SPECIES, shiftBy, 0, shiftMask);
  }

  private static VectorMask<Byte> sourceMask(final int offset, final int length) {
    // TODO Is there a way to get rid of this precondition? Measure performance impact
    if (length == 0) {
      throw new IndexOutOfBoundsException();
    }
    return SPECIES.indexInRange(offset, length);
  }

  /**
   * Returns the index of the last lane containing the first varint stored in the given vector.
   *
   * @param src the source vector
   * @return the index of the last lane
   * @throws IllegalArgumentException if the varint is malformed
   */
  private static int lastIndex(final ByteVector src) {
    // Every byte of a varint, except the last, has the most-significant bit set.
    // A lane value is negative if and only if the MSB is set. For a N-byte value,
    // the lanes in the range [offset..offset + N - 2] are set, the lane at index
    // offset + N - 1 is unset, and the remaining lanes may be set.
    final VectorMask<Byte> contMask = src.test(VectorOperators.IS_NEGATIVE);
    final int end = contMask.not().firstTrue();
    if (end >= MAX_BYTES) {
      throw new IllegalArgumentException("Found malformed varint");
    }
    return end;
  }

  /**
   * Reads a base-128 varint from the given vector in lanes {@code [0..end]}.
   *
   * @param src the source vector
   * @param end the index of the last lane
   * @return the read value
   */
  public static int read(final ByteVector src, final int end) {
    // Unset the most-significant bit and convert lane values from bytes to ints.
    final IntVector values = (IntVector) src.and(DROP_MSB)
        .convertShape(VectorOperators.B2I, INT_SPECIES, 0); // contraction

    // TODO Can we get rid of the last lanes without masking? Is it more performant?
    // Shift the value of each lane 7 * N positions to the left, where N is
    // the lane index. Finally, sum their values, discarding the remaining lanes.
    final VectorMask<Integer> varintMask = VARINT_MASKS[end];
    return values
        .lanewise(VectorOperators.LSHL, SHIFT_BY)
        .reduceLanes(VectorOperators.OR, varintMask);
  }

  /**
   * Reads a base-128 varint from the given array starting at the given offset, according to
   * {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order.
   *
   * @param src the array to read from
   * @param offset the offset into the array
   * @return the read value
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code offset >= src.length}
   * @throws IllegalArgumentException if the varint is malformed
   */
  public static int read(final byte[] src, final int offset) {
    final VectorMask<Byte> mask = sourceMask(offset, src.length);
    final ByteVector vector = ByteVector.fromArray(SPECIES, src, offset, mask);
    final int end = lastIndex(vector);
    return read(vector, end);
  }

  /**
   * Reads a base-128 varint from the given buffer starting at the given offset, according to
   * {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order.
   *
   * @param buffer the buffer to read from
   * @param offset the offset into the buffer
   * @return the read value
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code offset >=
   *     buffer.limit()}
   * @throws IllegalArgumentException if the varint is malformed
   */
  public static int read(final ByteBuffer buffer, final int offset) {
    final VectorMask<Byte> mask = sourceMask(offset, buffer.limit());
    // ByteVector.fromByteBuffer ignores the order argument
    final ByteVector src =
        ByteVector.fromByteBuffer(SPECIES, buffer, offset, ByteOrder.LITTLE_ENDIAN, mask);
    final int end = lastIndex(src);
    return read(src, end);
  }

  /**
   * Reads a base-128 varint from the given buffer starting at its current position, according to
   * {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order, and then increments the buffer's
   * position by the number of read bytes.
   *
   * @param buffer the buffer to read from
   * @return the read value
   * @throws IllegalArgumentException if the varint is malformed
   */
  public static int read(final ByteBuffer buffer) {
    final VectorMask<Byte> mask = sourceMask(buffer.position(), buffer.limit());
    final ByteVector vector = ByteVector.fromByteBuffer(SPECIES, buffer,
        buffer.position(), ByteOrder.LITTLE_ENDIAN, mask);
    final int end = lastIndex(vector);
    buffer.position(buffer.position() + end + 1);
    return read(vector, end);
  }

  /**
   * Writes the given value as a base-128 varint to the given array starting at the given offset,
   * according to {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order.
   *
   * @param dest the array to write to
   * @param offset the offset into the array
   * @param value the value to write
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code offset} is greater than
   *     {@code dest.length} minus the length of the encoded varint.
   */
  public static void write(final byte[] dest, final int offset, final int value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Writes the given value as a base-128 varint to the given buffer starting at the given offset,
   * according to {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order.
   *
   * @param dest the buffer to write to
   * @param offset the offset into the buffer
   * @param value the value to write
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code offset} is greater than
   *     the buffer's limit minus the length of the encoded varint.
   */
  public static void write(final ByteBuffer dest, final int offset, final int value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Writes the given value as a base-128 varint to the given array starting at its current
   * position, according to {@linkplain ByteOrder#LITTLE_ENDIAN little-endian} order, and then
   * increments the buffer's position by the number of written bytes.
   *
   * @param dest the buffer to write to
   * @param value the value to write
   * @throws IndexOutOfBoundsException if the buffer's position is greater than the buffer's
   *     limit minus the length of the encoded varint.
   */
  public static void write(final ByteBuffer dest, final int value) {
    throw new UnsupportedOperationException();
  }
}
