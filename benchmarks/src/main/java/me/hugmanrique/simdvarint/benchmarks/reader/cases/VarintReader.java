/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint.benchmarks.reader.cases;

import java.nio.ByteBuffer;

public interface VarintReader {

  default int read(final byte[] buffer, final int position) {
    throw new UnsupportedOperationException();
  }

  default int read(final ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }
}
