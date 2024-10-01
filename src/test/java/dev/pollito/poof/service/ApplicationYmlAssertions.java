package dev.pollito.poof.service;

import static dev.pollito.poof.service.GeneratePoofServiceTest.PROJECT_METADATA_ARTIFACT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.NotNull;

public class ApplicationYmlAssertions {
  private ApplicationYmlAssertions() {}

  public static void applicationYmlAssertions(@NotNull String applicationYmlContent) {
    assertTrue(
        applicationYmlContent.contains("name: " + PROJECT_METADATA_ARTIFACT),
        "application.yml should contain the correct spring application name");
    assertFalse(
        applicationYmlContent.contains("#artifact"),
        "application.yml should not contain the artifact marker");
  }
}
