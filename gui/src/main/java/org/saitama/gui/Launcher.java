package org.saitama.gui;

import javafx.application.Application;

/**
 * Entry point that does not extend {@link Application}, so the interface starts from a plain
 * classpath: the Java launcher refuses to start a JavaFX {@code Application} subclass directly
 * unless JavaFX is on the module path, and the packaged distribution ships JavaFX as ordinary
 * classpath jars.
 */
public final class Launcher {

  private Launcher() {}

  /** Launches the interface. */
  public static void main(String[] args) {
    Application.launch(SaitamaGui.class, args);
  }
}
