package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.NotNull;

public class ApplicationYmlAssertions {
  private ApplicationYmlAssertions() {}

  public static void applicationYmlAssertions(@NotNull String applicationYmlContent) {
    assertTrue(
        applicationYmlContent.contains("name: poof"),
        "application.yml should contain the correct spring application name");
    assertFalse(
        applicationYmlContent.contains("#artifact"),
        "application.yml should not contain the artifact marker");
  }
}
