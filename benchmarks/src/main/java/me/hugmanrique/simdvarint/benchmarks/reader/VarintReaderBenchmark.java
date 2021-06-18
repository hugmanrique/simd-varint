/*
 * Copyright (c) 2021 Hugo Manrique.
 *
 * This work is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package me.hugmanrique.simdvarint.benchmarks.reader;

import static me.hugmanrique.simdvarint.benchmarks.BenchmarkUtils.MAX_VARINT_BYTES;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import me.hugmanrique.simdvarint.benchmarks.BenchmarkUtils;
import me.hugmanrique.simdvarint.benchmarks.reader.cases.ProtobufVarintReader;
import me.hugmanrique.simdvarint.benchmarks.reader.cases.SimdVarintReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5) // s
@Measurement(iterations = 5, time = 5) // s
@Fork(1)
@State(Scope.Benchmark)
@SuppressWarnings("DuplicatedCode") // TODO Tidy up
public class VarintReaderBenchmark {

  private static final int BUF_COUNT = 2048;
  private static final int BUF_SIZE = 256;

  private byte[][] arrays;
  private ByteBuffer[] buffers;
  private int[] positions;

  @Setup
  public void setup() {
    this.arrays = new byte[BUF_COUNT][BUF_SIZE];
    this.buffers = new ByteBuffer[BUF_COUNT];
    this.positions = new int[BUF_COUNT];

    final Random random = new Random();
    for (int i = 0; i < BUF_COUNT; i++) {
      this.positions[i] = random.nextInt(BUF_SIZE - MAX_VARINT_BYTES);
      this.buffers[i] = ByteBuffer.wrap(this.arrays[i]).position(this.positions[i]);
      random.nextBytes(this.arrays[i]);

      final int bitCount = random.nextInt(30) + 1;
      final int value = BenchmarkUtils.generateRandomBitNumber(random, bitCount);
      BenchmarkUtils.writeVarint(this.arrays[i], this.positions[i], value);
    }
  }

  @Benchmark
  public int protobufArrayReader(final ProtobufVarintReader reader) {
    int sum = 0;
    for (int i = 0; i < BUF_COUNT; i++) {
      final int value = reader.read(this.arrays[i], this.positions[i]);
      sum += value;
    }
    return sum;
  }

  @Benchmark
  public int protobufBufferReader(final ProtobufVarintReader reader) {
    int sum = 0;
    for (int i = 0; i < BUF_COUNT; i++) {
      final int value = reader.read(this.buffers[i]);
      sum += value;
      this.buffers[i].position(this.positions[i]);
    }
    return sum;
  }

  @Benchmark
  public int simdArrayReader(final SimdVarintReader reader) {
    int sum = 0;
    for (int i = 0; i < BUF_COUNT; i++) {
      final int value = reader.read(this.arrays[i], this.positions[i]);
      sum += value;
    }
    return sum;
  }

  @Benchmark
  public int simdBufferReader(final SimdVarintReader reader) {
    int sum = 0;
    for (int i = 0; i < BUF_COUNT; i++) {
      final int value = reader.read(this.buffers[i]);
      sum += value;
      this.buffers[i].position(this.positions[i]);
    }
    return sum;
  }

  public static void main(final String[] args) throws RunnerException {
    final Options opt = new OptionsBuilder()
        .include(VarintReaderBenchmark.class.getSimpleName())
        .jvmArgsAppend("--add-modules", "jdk.incubator.vector")
        .build();

    new Runner(opt).run();
  }
}
