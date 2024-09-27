package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationYmlAssertions {
  private ApplicationYmlAssertions() {}

  public static void applicationYmlAssertions(String applicationYmlContent) {
    assertNotNull(applicationYmlContent, "application.yml content should not be null");
    assertTrue(
        applicationYmlContent.contains("name: poof"),
        "application.yml should contain the correct spring application name");
  }
}
