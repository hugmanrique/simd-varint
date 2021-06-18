package me.hugmanrique.simdvarint.benchmarks.reader.cases;

import java.nio.ByteBuffer;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * @see <a href="https://github.com/protocolbuffers/protobuf/blob/c67c6cebc816f14481bc94c813ec534024f8dfbc/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L988">Original</a>
 */
@State(Scope.Benchmark)
public class ProtobufVarintReader implements VarintReader {

  @Override
  public int read(final byte[] buffer, int position) {
    if (buffer.length == position) {
      throw new IllegalArgumentException();
    }

    int value;
    if ((value = buffer[position++]) >= 0) {
      return value;
    } else if (buffer.length - position < 9) {
      throw new IllegalArgumentException();
    } else if ((value ^= (buffer[position++] << 7)) < 0) {
      value ^= (~0 << 7);
    } else if ((value ^= (buffer[position++] << 14)) >= 0) {
      value ^= (~0 << 7) ^ (~0 << 14);
    } else if ((value ^= (buffer[position++] << 21)) < 0) {
      value ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
    } else {
      int y = buffer[position++];
      value ^= y << 28;
      value ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
      if (y < 0
          && buffer[position++] < 0
          && buffer[position++] < 0
          && buffer[position++] < 0
          && buffer[position++] < 0
          && buffer[position] < 0) {
        throw new IllegalArgumentException();
      }
    }
    return value;
  }

  @Override
  public int read(final ByteBuffer buffer) {
    int tempPos = buffer.position();
    if (buffer.limit() == tempPos) {
      throw new IllegalArgumentException();
    }

    int value;
    if ((value = buffer.get(tempPos++)) >= 0) {
      buffer.position(tempPos);
      return value;
    } else if (buffer.limit() - tempPos < 9) {
      throw new IllegalArgumentException();
    } else if ((value ^= (buffer.get(tempPos++) << 7)) < 0) {
      value ^= (~0 << 7);
    } else if ((value ^= (buffer.get(tempPos++) << 14)) >= 0) {
      value ^= (~0 << 7) ^ (~0 << 14);
    } else if ((value ^= (buffer.get(tempPos++) << 21)) < 0) {
      value ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
    } else {
      int y = buffer.get(tempPos++);
      value ^= y << 28;
      value ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
      if (y < 0
          && buffer.get(tempPos++) < 0
          && buffer.get(tempPos++) < 0
          && buffer.get(tempPos++) < 0
          && buffer.get(tempPos++) < 0
          && buffer.get(tempPos++) < 0) {
        throw new IllegalArgumentException();
      }
    }
    buffer.position(tempPos);
    return value;
  }
}
