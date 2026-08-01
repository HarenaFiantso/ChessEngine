package org.saitama.search;

/**
 * Unwinds a search whose stop signal fired; the partial iteration's answer must be discarded,
 * because an interrupted alpha-beta pass may not have finished refuting the current best move.
 */
final class SearchAborted extends RuntimeException {

  SearchAborted() {
    super("Search aborted by its stop signal", null, false, false);
  }
}
