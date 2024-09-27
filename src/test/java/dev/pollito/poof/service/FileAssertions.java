package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class FileAssertions {
  private FileAssertions() {}

  public static void checkAllExpectedFilesWereCopied(
      @NotNull Map<String, Boolean> expectedEntryNames) {
    expectedEntryNames.forEach(
        (entryName, isFound) -> assertTrue(isFound, entryName + " should exist"));
  }

  public static void checkFileIsNotEmpty(String entryName, @NotNull String fileContent) {
    assertFalse(fileContent.trim().isEmpty(), entryName + " should not be empty");
  }

  public static void checkFileIsExpected(
      @NotNull Map<String, Boolean> expectedEntryNames, String entryName) {
    assertTrue(expectedEntryNames.containsKey(entryName), "Unexpected file: " + entryName);
  }
}
