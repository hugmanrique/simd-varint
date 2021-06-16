# simd-varint

[![artifact][artifact]][artifact-url]
[![javadoc][javadoc]][javadoc-url]
<!--[![tests][tests]][tests-url]-->
[![license][license]][license-url]

Provides methods to read and write [variable-length integers](https://developers.google.com/protocol-buffers/docs/encoding)
employing _Single Instruction Multiple Data_ (SIMD) parallelism, using the incubating [Vector API](https://openjdk.java.net/jeps/338).

## Installation

Requires OpenJDK [build 17-panama+3-167](https://jdk.java.net/panama/) or higher.

### Gradle

```groovy
repositories {
  mavenCentral()
}

dependencies {
  compile 'me.hugmanrique:simd-varint:0.0.1-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
  <groupId>me.hugmanrique</groupId>
  <artifactId>simd-varint</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage

```java
final byte[] buf = new byte[] { (byte) 0xFF, 0x01 };
assertEquals(255, VarInts.read(buf, 0));
```

## License

[MIT](LICENSE) &copy; [Hugo Manrique](https://hugmanrique.me)

[artifact]: https://img.shields.io/maven-central/v/me.hugmanrique/simd-varint
[artifact-url]: https://search.maven.org/artifact/me.hugmanrique/simd-varint
[javadoc]: https://javadoc.io/badge2/me.hugmanrique/simd-varint/javadoc.svg
[javadoc-url]: https://javadoc.io/doc/me.hugmanrique/simd-varint
<!--[tests]: https://img.shields.io/travis/hugmanrique/simd-varint/main.svg
[tests-url]: https://travis-ci.org/hugmanrique/simd-varint-->
[license]: https://img.shields.io/github/license/hugmanrique/simd-varint.svg
[license-url]: LICENSE
