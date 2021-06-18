# Benchmarks

Heavily inspired by [varint-writing-showdown](https://github.com/astei/varint-writing-showdown) by
A. Steinborn and contributors, this project benchmarks varint reading implementations.

To run the benchmarks:

```shell
./gradlew run
```

## Results
Last updated on 2021-06-18.

```
Benchmark                                    Mode  Cnt       Score      Error  Units
VarintReaderBenchmark.protobufArrayReader   thrpt    5  297220,384 ± 1004,611  ops/s
VarintReaderBenchmark.protobufBufferReader  thrpt    5   99298,189 ±  556,401  ops/s
VarintReaderBenchmark.simdArrayReader       thrpt    5   29035,811 ±  107,601  ops/s
VarintReaderBenchmark.simdBufferReader      thrpt    5   25393,417 ±  475,116  ops/s
```
