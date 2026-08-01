package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CastlingRightsTest {

  @ParameterizedTest
  @EnumSource(CastlingRight.class)
  void allRetainsEveryRight(CastlingRight right) {
    assertTrue(CastlingRights.all().allows(right));
  }

  @ParameterizedTest
  @EnumSource(CastlingRight.class)
  void noneRetainsNoRight(CastlingRight right) {
    assertFalse(CastlingRights.none().allows(right));
  }

  @Test
  void ofRetainsExactlyTheGivenRights() {
    CastlingRights rights = CastlingRights.of(CastlingRight.WHITE_KINGSIDE);
    assertTrue(rights.allows(CastlingRight.WHITE_KINGSIDE));
    assertFalse(rights.allows(CastlingRight.WHITE_QUEENSIDE));
    assertFalse(rights.allows(CastlingRight.BLACK_KINGSIDE));
    assertFalse(rights.allows(CastlingRight.BLACK_QUEENSIDE));
  }

  @Test
  void ofRejectsDuplicateRights() {
    assertThrows(
        IllegalArgumentException.class,
        () -> CastlingRights.of(CastlingRight.WHITE_KINGSIDE, CastlingRight.WHITE_KINGSIDE));
  }

  @Test
  void equalityFollowsTheRetainedSetRegardlessOfArgumentOrder() {
    assertEquals(
        CastlingRights.of(CastlingRight.WHITE_KINGSIDE, CastlingRight.BLACK_QUEENSIDE),
        CastlingRights.of(CastlingRight.BLACK_QUEENSIDE, CastlingRight.WHITE_KINGSIDE));
  }

  @Test
  void exposedRightsAreImmutable() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> CastlingRights.all().rights().remove(CastlingRight.WHITE_KINGSIDE));
  }
}
