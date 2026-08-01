package org.saitama;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.saitama.uci.UciEngine;

/** Command-line entry point: the engine speaks UCI on standard input and output. */
public class Main {
  static void main() throws IOException {
    new UciEngine(new InputStreamReader(System.in, StandardCharsets.UTF_8), IO::println).run();
  }
}
