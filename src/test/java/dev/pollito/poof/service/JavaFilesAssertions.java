package dev.pollito.poof.service;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;
import static dev.pollito.poof.service.GeneratePoofServiceTest.PROJECT_METADATA_ARTIFACT;
import static dev.pollito.poof.service.GeneratePoofServiceTest.PROJECT_METADATA_GROUP;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.PoofRequest;
import org.jetbrains.annotations.NotNull;

public class JavaFilesAssertions {
  private JavaFilesAssertions() {}

  public static void javaFilesAssertions(
      PoofRequest request, @NotNull String entryName, @NotNull String javaFileContent) {
    assertTrue(
        javaFileContent.startsWith(
            "package " + PROJECT_METADATA_GROUP + "." + PROJECT_METADATA_ARTIFACT),
        entryName
            + " should start with 'package "
            + PROJECT_METADATA_GROUP
            + "."
            + PROJECT_METADATA_ARTIFACT
            + "'");

    if (entryName.equals(
        "src/main/java/"
            + PROJECT_METADATA_GROUP.replace(".", "/")
            + "/"
            + PROJECT_METADATA_ARTIFACT
            + "/PoofApplication.java")) {
      mainJavaFileAssertions(javaFileContent);
    }
    if (entryName.equals(
        "src/test/java/"
            + PROJECT_METADATA_GROUP.replace(".", "/")
            + "/"
            + PROJECT_METADATA_ARTIFACT
            + "/PoofApplicationTests.java")) {
      appTestFileAssertions(javaFileContent);
    }
    if (entryName.equals(
        "src/main/java/"
            + PROJECT_METADATA_GROUP.replace(".", "/")
            + "/"
            + PROJECT_METADATA_ARTIFACT
            + "/aspect/LoggingAspect.java")) {
      aspectAssertions(request, javaFileContent);
    }
  }

  private static void aspectAssertions(@NotNull PoofRequest request, String aspectContent) {
    if (request.getOptions().getLoggingAspect()) {
      assertNotNull(aspectContent, "LoggingAspect.java should exist");
      assertTrue(
          aspectContent.contains("public class LoggingAspect"),
          "LoggingAspect.java should contain the correct class name");
      assertTrue(
          aspectContent.contains(
              "@Pointcut(\"execution(public * "
                  + PROJECT_METADATA_GROUP
                  + "."
                  + PROJECT_METADATA_ARTIFACT
                  + ".controller..*.*(..))\")"),
          "LoggingAspect.java should contain the correct pointcut expression");
    } else {
      assertNull(aspectContent, "LoggingAspect.java should not exist");
    }
  }

  private static void appTestFileAssertions(String appTestFileContent) {
    assertNotNull(
        appTestFileContent,
        capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT)
            + "ApplicationTests.java content should not be null");
    assertTrue(
        appTestFileContent.contains(
            "class " + capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT) + "ApplicationTests {"),
        "Main Java application test file should contain the correct class name");
  }

  private static void mainJavaFileAssertions(String mainJavaAppFileContent) {
    assertNotNull(
        mainJavaAppFileContent,
        capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT)
            + "Application.java content should not be null");
    assertTrue(
        mainJavaAppFileContent.contains(
            "public class " + capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT) + "Application {"),
        "Main Java application file should contain the correct class name");
    assertTrue(
        mainJavaAppFileContent.contains(
            "SpringApplication.run("
                + capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT)
                + "Application.class, args);"),
        "Main Java application file should run the correct SpringApplication.run");
  }
}
