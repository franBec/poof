package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.impl.GeneratePoofServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneratePoofServiceTest {
  public static final String PROJECT_METADATA_GROUP = "dev.pollito";
  public static final String PROJECT_METADATA_ARTIFACT = "poof";
  public static final String PROJECT_METADATA_DESCRIPTION =
      "poof - Pollito Over Opinionated Framework";
  @InjectMocks private GeneratePoofServiceImpl generatePoofService;

  @Test
  @SneakyThrows
  void generatedZipContainsExpectedFiles() {
    GenerateRequest request =
        new GenerateRequest()
            .projectMetadata(
                new ProjectMetadata()
                    .group(PROJECT_METADATA_GROUP)
                    .artifact(PROJECT_METADATA_ARTIFACT)
                    .description(PROJECT_METADATA_DESCRIPTION))
            .options(new Options().loggingAspect(true));

    Map<String, Boolean> expectedEntryNames = buildExpectedEntryNamesMap(request);

    ByteArrayOutputStream zipOutputStream = generatePoofService.generateFiles(request);

    try (ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(zipOutputStream.toByteArray()))) {

      ZipEntry entry;
      while (Objects.nonNull(entry = zipInputStream.getNextEntry())) {
        String entryName = entry.getName();
        assertTrue(expectedEntryNames.containsKey(entryName), "Unexpected file: " + entryName);

        if (entryName.equals("pom.xml")) {
          pomXmlAssertions(request, readZipEntryContent(zipInputStream));
        }

        if (entryName.equals("src/main/resources/application.yml")) {
          applicationYmlAssertions(readZipEntryContent(zipInputStream));
        }
        if (entryName.endsWith(".java")) {
          javaFilesAssertions(request, entryName, readZipEntryContent(zipInputStream));
        }
        expectedEntryNames.put(entryName, true);
        zipInputStream.closeEntry();
      }

      expectedEntryNames.forEach(
          (entryName, isFound) -> assertTrue(isFound, entryName + " should exist"));
    }
  }

  private void javaFilesAssertions(
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
  }

  @NotNull
  private Map<String, Boolean> buildExpectedEntryNamesMap(@NotNull GenerateRequest request) {
    Map<String, Boolean> expectedEntryNames = new HashMap<>();
    expectedEntryNames.put(".mvn/wrapper/maven-wrapper.properties", false);
    if (request.getOptions().getLoggingAspect()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/aspect/LoggingAspect.java", false);
    }
    expectedEntryNames.put("src/main/java/dev/pollito/poof/PoofApplication.java", false);
    expectedEntryNames.put("src/main/resources/application.yml", false);
    expectedEntryNames.put("src/test/java/dev/pollito/poof/PoofApplicationTests.java", false);
    expectedEntryNames.put(".gitignore", false);
    expectedEntryNames.put("HELP.md", false);
    expectedEntryNames.put("mvnw", false);
    expectedEntryNames.put("mvnw.cmd", false);
    expectedEntryNames.put("pom.xml", false);
    return expectedEntryNames;
  }

  private void aspectAssertions(@NotNull GenerateRequest request, String aspectContent) {
    if (Boolean.TRUE.equals(request.getOptions().getLoggingAspect())) {
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

  private void applicationYmlAssertions(String applicationYmlContent) {
    assertNotNull(applicationYmlContent, "application.yml content should not be null");
    assertTrue(
        applicationYmlContent.contains("name: poof"),
        "application.yml should contain the correct spring application name");
  }

  private void appTestFileAssertions(String appTestFileContent) {
    assertNotNull(appTestFileContent, "PoofApplicationTests.java content should not be null");
    assertTrue(
        appTestFileContent.contains("class PoofApplicationTests {"),
        "Main Java application test file should contain the correct class name");
  }

  private void mainJavaFileAssertions(String mainJavaAppFileContent) {
    assertNotNull(mainJavaAppFileContent, "PoofApplication.java content should not be null");
    assertTrue(
        mainJavaAppFileContent.contains("public class PoofApplication {"),
        "Main Java application file should contain the correct class name");
    assertTrue(
        mainJavaAppFileContent.contains("SpringApplication.run(PoofApplication.class, args);"),
        "Main Java application file should run the correct SpringApplication.run");
  }

  private void pomXmlAssertions(@NotNull GenerateRequest request, String pomXmlContent) {
    assertNotNull(pomXmlContent, "pom.xml content should not be null");
    assertTrue(
        pomXmlContent.contains("<groupId>dev.pollito</groupId>"),
        "pom.xml should contain the correct <groupId>");
    assertTrue(
        pomXmlContent.contains("<artifactId>poof</artifactId>"),
        "pom.xml should contain the correct <artifactId>");
    assertTrue(
        pomXmlContent.contains("<name>poof</name>"), "pom.xml should contain the correct <name>");
    assertTrue(
        pomXmlContent.contains(
            "<description>poof - Pollito Over Opinionated Framework</description>"),
        "pom.xml should contain the correct <description>");
    String aspectDependency =
        "\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.aspectj</groupId>\r\n\t\t\t<artifactId>aspectjtools</artifactId>\r\n\t\t\t<version>1.9.22.1</version>\r\n\t\t</dependency>";
    if (Boolean.TRUE.equals(request.getOptions().getLoggingAspect())) {
      assertTrue(
          pomXmlContent.contains(aspectDependency), "pom.xml should contain aspect dependency");
    } else {
      assertFalse(
          pomXmlContent.contains(aspectDependency), "pom.xml should not contain aspect dependency");
    }
    assertTrue(
        pomXmlContent.contains("<id>provider generation - poof.yaml</id>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution id");
    assertTrue(
        pomXmlContent.contains(
            "<inputSpec>${project.basedir}/src/main/resources/openapi/poof.yaml</inputSpec>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration inputSpec");
    assertTrue(
        pomXmlContent.contains("<apiPackage>${project.groupId}.poof.api</apiPackage>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration apiPackage");
    assertTrue(
        pomXmlContent.contains("<modelPackage>${project.groupId}.poof.model</modelPackage>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration modelPackage");
  }

  @SneakyThrows
  private String readZipEntryContent(@NotNull InputStream inputStream) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, length);
    }
    return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
  }

  @Test
  void generatePoofThrowsException() {
    GenerateRequest generateRequest = new GenerateRequest();
    assertThrows(RuntimeException.class, () -> generatePoofService.generateFiles(generateRequest));
  }
}
