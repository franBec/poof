package dev.pollito.poof.service;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.GenerateRequest;
import org.jetbrains.annotations.NotNull;

public class JavaFilesAssertions {
  private JavaFilesAssertions() {}

  public static void javaFilesAssertions(
      GenerateRequest request, @NotNull String entryName, @NotNull String javaFileContent) {
    assertTrue(
        javaFileContent.startsWith("package dev.pollito.poof"),
        entryName + " should start with 'package dev.pollito.poof'");

    if (entryName.equals("src/main/java/dev/pollito/poof/PoofApplication.java")) {
      mainJavaFileAssertions(javaFileContent);
    }
    if (entryName.equals("src/test/java/dev/pollito/poof/PoofApplicationTests.java")) {
      appTestFileAssertions(javaFileContent);
    }
    if (entryName.equals("src/main/java/dev/pollito/poof/aspect/LoggingAspect.java")) {
      aspectAssertions(request, javaFileContent);
    }
    if (entryName.equals(
        "src/main/java/dev/pollito/poof/controller/advice/GlobalControllerAdvice.java")) {
      controllerAdviceAssertions(request, javaFileContent);
    }
    if (entryName.startsWith("src/main/java/dev/pollito/poof/errordecoder/")) {
      consumerErrorDecoderAssertions(request, entryName, javaFileContent);
    }
    if (entryName.startsWith("src/main/java/dev/pollito/poof/exception/")) {
      consumerExceptionAssertions(request, entryName, javaFileContent);
    }
  }

  private static void consumerExceptionAssertions(
      @NotNull GenerateRequest request,
      @NotNull String entryName,
      @NotNull String javaFileContent) {
    long fileNameCount =
        request.getContracts().getConsumerContracts().stream()
            .map(
                contract ->
                    "src/main/java/dev/pollito/poof/exception/"
                        + capitalizeFirstLetter(contract.getName())
                        + "Exception.java")
            .filter(entryName::contains)
            .count();
    assertEquals(
        1,
        fileNameCount,
        "Consumer Exception generated should contain one match of the possible names, but found "
            + fileNameCount);

    long classDefinitionCount =
        request.getContracts().getConsumerContracts().stream()
            .map(
                contract ->
                    "public class "
                        + capitalizeFirstLetter(contract.getName())
                        + "Exception extends RuntimeException {")
            .filter(javaFileContent::contains)
            .count();

    assertEquals(
        1,
        classDefinitionCount,
        "Consumer Exception generated should contain one match of the possible class definition, but found "
            + classDefinitionCount);
  }

  private static void consumerErrorDecoderAssertions(
      @NotNull GenerateRequest request,
      @NotNull String entryName,
      @NotNull String javaFileContent) {
    long fileNameCount =
        request.getContracts().getConsumerContracts().stream()
            .map(
                contract ->
                    "src/main/java/dev/pollito/poof/errordecoder/"
                        + capitalizeFirstLetter(contract.getName())
                        + "ErrorDecoder.java")
            .filter(entryName::contains)
            .count();
    assertEquals(
        1,
        fileNameCount,
        "Consumer Error Decoder generated should contain one match of the possible names, but found "
            + fileNameCount);

    long classDefinitionCount =
        request.getContracts().getConsumerContracts().stream()
            .map(
                contract ->
                    "public class "
                        + capitalizeFirstLetter(contract.getName())
                        + "ErrorDecoder implements ErrorDecoder {")
            .filter(javaFileContent::contains)
            .count();

    assertEquals(
        1,
        classDefinitionCount,
        "Error Decoder generated should contain one match of the possible class definition, but found "
            + classDefinitionCount);

    long exceptionReturnedCount =
        request.getContracts().getConsumerContracts().stream()
            .map(
                contract ->
                    "return new "
                        + capitalizeFirstLetter(contract.getName())
                        + "Exception(new String(body.readAllBytes(), StandardCharsets.UTF_8));")
            .filter(javaFileContent::contains)
            .count();

    assertEquals(
        1,
        classDefinitionCount,
        "Error Decoder generated should contain one match of the possible exception returns, but found "
            + exceptionReturnedCount);
  }

  private static void controllerAdviceAssertions(
      @NotNull GenerateRequest request, @NotNull String javaFileContent) {
    assertFalse(
        javaFileContent.contains("/*ConsumerExceptionImports*/"),
        "GlobalControllerAdvice.java should not contain /*ConsumerExceptionImports*/");
    assertFalse(
        javaFileContent.contains("/*ConsumerExceptionHandlers*/"),
        "GlobalControllerAdvice.java should not contain /*ConsumerExceptionHandlers*/");

    request
        .getContracts()
        .getConsumerContracts()
        .forEach(
            contract ->
                assertTrue(
                    javaFileContent.contains(
                        "import dev.pollito.poof.exception."
                            + capitalizeFirstLetter(contract.getName())
                            + "Exception;"),
                    "GlobalControllerAdvice.java should contain import dev.pollito.poof.exception."
                        + capitalizeFirstLetter(contract.getName())
                        + "Exception;"));
  }

  private static void aspectAssertions(@NotNull GenerateRequest request, String aspectContent) {
    if (request.getOptions().getLoggingAspect()) {
      assertNotNull(aspectContent, "LoggingAspect.java should exist");
      assertTrue(
          aspectContent.contains("public class LoggingAspect"),
          "LoggingAspect.java should contain the correct class name");
      assertTrue(
          aspectContent.contains(
              "@Pointcut(\"execution(public * dev.pollito.poof.controller..*.*(..))\")"),
          "LoggingAspect.java should contain the correct pointcut expression");
    } else {
      assertNull(aspectContent, "LoggingAspect.java should not exist");
    }
  }

  private static void appTestFileAssertions(String appTestFileContent) {
    assertNotNull(appTestFileContent, "PoofApplicationTests.java content should not be null");
    assertTrue(
        appTestFileContent.contains("class PoofApplicationTests {"),
        "Main Java application test file should contain the correct class name");
  }

  private static void mainJavaFileAssertions(String mainJavaAppFileContent) {
    assertNotNull(mainJavaAppFileContent, "PoofApplication.java content should not be null");
    assertTrue(
        mainJavaAppFileContent.contains("public class PoofApplication {"),
        "Main Java application file should contain the correct class name");
    assertTrue(
        mainJavaAppFileContent.contains("SpringApplication.run(PoofApplication.class, args);"),
        "Main Java application file should run the correct SpringApplication.run");
  }
}
