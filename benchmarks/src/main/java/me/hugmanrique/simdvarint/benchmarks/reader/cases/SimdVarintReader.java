/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint.benchmarks.reader.cases;

import java.nio.ByteBuffer;
import me.hugmanrique.simdvarint.Varints;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class SimdVarintReader implements VarintReader {

  @Override
  public int read(final byte[] buffer, final int position) {
    return Varints.read(buffer, position);
  }

  @Override
  public int read(final ByteBuffer buffer) {
    return Varints.read(buffer);
  }
}
