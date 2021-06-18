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
