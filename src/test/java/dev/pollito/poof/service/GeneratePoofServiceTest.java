package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.Contracts;
import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.impl.GeneratePoofServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneratePoofServiceTest {
  @InjectMocks private GeneratePoofServiceImpl generatePoofService;

  private static final String PROJECT_METADATA_GROUP = "dev.pollito";
  private static final String PROJECT_METADATA_ARTIFACT = "poof";
  private static final String PROJECT_METADATA_DESCRIPTION =
      "poof - Pollito Over Opinionated Framework";
  private static final String BASE_64_OAS_YAML =
      "b3BlbmFwaTogIjMuMC4wIgppbmZvOgogIHZlcnNpb246IDEuMC4wCiAgdGl0bGU6IFN3YWdnZXIgUGV0c3RvcmUKICBsaWNlbnNlOgogICAgbmFtZTogTUlUCnNlcnZlcnM6CiAgLSB1cmw6IGh0dHA6Ly9wZXRzdG9yZS5zd2FnZ2VyLmlvL3YxCnBhdGhzOgogIC9wZXRzOgogICAgZ2V0OgogICAgICBzdW1tYXJ5OiBMaXN0IGFsbCBwZXRzCiAgICAgIG9wZXJhdGlvbklkOiBsaXN0UGV0cwogICAgICB0YWdzOgogICAgICAgIC0gcGV0cwogICAgICBwYXJhbWV0ZXJzOgogICAgICAgIC0gbmFtZTogbGltaXQKICAgICAgICAgIGluOiBxdWVyeQogICAgICAgICAgZGVzY3JpcHRpb246IEhvdyBtYW55IGl0ZW1zIHRvIHJldHVybiBhdCBvbmUgdGltZSAobWF4IDEwMCkKICAgICAgICAgIHJlcXVpcmVkOiBmYWxzZQogICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICB0eXBlOiBpbnRlZ2VyCiAgICAgICAgICAgIG1heGltdW06IDEwMAogICAgICAgICAgICBmb3JtYXQ6IGludDMyCiAgICAgIHJlc3BvbnNlczoKICAgICAgICAnMjAwJzoKICAgICAgICAgIGRlc2NyaXB0aW9uOiBBIHBhZ2VkIGFycmF5IG9mIHBldHMKICAgICAgICAgIGhlYWRlcnM6CiAgICAgICAgICAgIHgtbmV4dDoKICAgICAgICAgICAgICBkZXNjcmlwdGlvbjogQSBsaW5rIHRvIHRoZSBuZXh0IHBhZ2Ugb2YgcmVzcG9uc2VzCiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgdHlwZTogc3RyaW5nCiAgICAgICAgICBjb250ZW50OgogICAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOiAgICAKICAgICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvUGV0cyIKICAgICAgICBkZWZhdWx0OgogICAgICAgICAgZGVzY3JpcHRpb246IHVuZXhwZWN0ZWQgZXJyb3IKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL0Vycm9yIgogICAgcG9zdDoKICAgICAgc3VtbWFyeTogQ3JlYXRlIGEgcGV0CiAgICAgIG9wZXJhdGlvbklkOiBjcmVhdGVQZXRzCiAgICAgIHRhZ3M6CiAgICAgICAgLSBwZXRzCiAgICAgIHJlcXVlc3RCb2R5OgogICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOgogICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL1BldCcKICAgICAgICByZXF1aXJlZDogdHJ1ZQogICAgICByZXNwb25zZXM6CiAgICAgICAgJzIwMSc6CiAgICAgICAgICBkZXNjcmlwdGlvbjogTnVsbCByZXNwb25zZQogICAgICAgIGRlZmF1bHQ6CiAgICAgICAgICBkZXNjcmlwdGlvbjogdW5leHBlY3RlZCBlcnJvcgogICAgICAgICAgY29udGVudDoKICAgICAgICAgICAgYXBwbGljYXRpb24vanNvbjoKICAgICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvRXJyb3IiCiAgL3BldHMve3BldElkfToKICAgIGdldDoKICAgICAgc3VtbWFyeTogSW5mbyBmb3IgYSBzcGVjaWZpYyBwZXQKICAgICAgb3BlcmF0aW9uSWQ6IHNob3dQZXRCeUlkCiAgICAgIHRhZ3M6CiAgICAgICAgLSBwZXRzCiAgICAgIHBhcmFtZXRlcnM6CiAgICAgICAgLSBuYW1lOiBwZXRJZAogICAgICAgICAgaW46IHBhdGgKICAgICAgICAgIHJlcXVpcmVkOiB0cnVlCiAgICAgICAgICBkZXNjcmlwdGlvbjogVGhlIGlkIG9mIHRoZSBwZXQgdG8gcmV0cmlldmUKICAgICAgICAgIHNjaGVtYToKICAgICAgICAgICAgdHlwZTogc3RyaW5nCiAgICAgIHJlc3BvbnNlczoKICAgICAgICAnMjAwJzoKICAgICAgICAgIGRlc2NyaXB0aW9uOiBFeHBlY3RlZCByZXNwb25zZSB0byBhIHZhbGlkIHJlcXVlc3QKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL1BldCIKICAgICAgICBkZWZhdWx0OgogICAgICAgICAgZGVzY3JpcHRpb246IHVuZXhwZWN0ZWQgZXJyb3IKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL0Vycm9yIgpjb21wb25lbnRzOgogIHNjaGVtYXM6CiAgICBQZXQ6CiAgICAgIHR5cGU6IG9iamVjdAogICAgICByZXF1aXJlZDoKICAgICAgICAtIGlkCiAgICAgICAgLSBuYW1lCiAgICAgIHByb3BlcnRpZXM6CiAgICAgICAgaWQ6CiAgICAgICAgICB0eXBlOiBpbnRlZ2VyCiAgICAgICAgICBmb3JtYXQ6IGludDY0CiAgICAgICAgbmFtZToKICAgICAgICAgIHR5cGU6IHN0cmluZwogICAgICAgIHRhZzoKICAgICAgICAgIHR5cGU6IHN0cmluZwogICAgUGV0czoKICAgICAgdHlwZTogYXJyYXkKICAgICAgbWF4SXRlbXM6IDEwMAogICAgICBpdGVtczoKICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvUGV0IgogICAgRXJyb3I6CiAgICAgIHR5cGU6IG9iamVjdAogICAgICByZXF1aXJlZDoKICAgICAgICAtIGNvZGUKICAgICAgICAtIG1lc3NhZ2UKICAgICAgcHJvcGVydGllczoKICAgICAgICBjb2RlOgogICAgICAgICAgdHlwZTogaW50ZWdlcgogICAgICAgICAgZm9ybWF0OiBpbnQzMgogICAgICAgIG1lc3NhZ2U6CiAgICAgICAgICB0eXBlOiBzdHJpbmcK";

  private static Stream<Options> optionsProvider() {
    List<Options> optionsList = new ArrayList<>();
    for (boolean allowCors : new boolean[] {true, false}) {
      for (boolean controllerAdvice : new boolean[] {true, false}) {
        for (boolean logFilter : new boolean[] {true, false}) {
          for (boolean loggingAspect : new boolean[] {true, false}) {
            optionsList.add(
                new Options()
                    .allowCorsFromAnySource(allowCors)
                    .controllerAdvice(controllerAdvice)
                    .logFilter(logFilter)
                    .loggingAspect(loggingAspect));
          }
        }
      }
    }
    return optionsList.stream();
  }

  @ParameterizedTest
  @MethodSource("optionsProvider")
  @SneakyThrows
  void generatedZipContainsExpectedFiles(Options options) {
    GenerateRequest request =
        new GenerateRequest()
            .contracts(new Contracts().providerContract(BASE_64_OAS_YAML))
            .projectMetadata(
                new ProjectMetadata()
                    .group(PROJECT_METADATA_GROUP)
                    .artifact(PROJECT_METADATA_ARTIFACT)
                    .description(PROJECT_METADATA_DESCRIPTION))
            .options(options);

    Map<String, Boolean> expectedEntryNames = buildExpectedEntryNamesMap(request);

    try (ZipInputStream zipInputStream =
        new ZipInputStream(
            new ByteArrayInputStream(generatePoofService.generateFiles(request).toByteArray()))) {

      ZipEntry entry;
      while (Objects.nonNull(entry = zipInputStream.getNextEntry())) {
        String entryName = entry.getName();
        checkFileIsExpected(expectedEntryNames, entryName);

        if (entryName.equals("pom.xml")) {
          pomXmlAssertions(request, readZipEntryContent(zipInputStream));
        } else if (entryName.equals("src/main/resources/application.yml")) {
          applicationYmlAssertions(readZipEntryContent(zipInputStream));
        } else if (entryName.endsWith(".java")) {
          javaFilesAssertions(request, entryName, readZipEntryContent(zipInputStream));
        } else {
          checkFileIsNotEmpty(entryName, readZipEntryContent(zipInputStream));
        }
        expectedEntryNames.put(entryName, true);
        zipInputStream.closeEntry();
      }

      checkAllExpectedFilesWereCopied(expectedEntryNames);
    }
  }

  private void checkAllExpectedFilesWereCopied(@NotNull Map<String, Boolean> expectedEntryNames) {
    expectedEntryNames.forEach(
        (entryName, isFound) -> assertTrue(isFound, entryName + " should exist"));
  }

  private void checkFileIsExpected(
      @NotNull Map<String, Boolean> expectedEntryNames, String entryName) {
    assertTrue(expectedEntryNames.containsKey(entryName), "Unexpected file: " + entryName);
  }

  private void checkFileIsNotEmpty(String entryName, @NotNull String fileContent) {
    assertFalse(fileContent.trim().isEmpty(), entryName + " should not be empty");
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
    expectedEntryNames.put("src/main/java/dev/pollito/poof/PoofApplication.java", false);
    expectedEntryNames.put("src/main/resources/application.yml", false);
    expectedEntryNames.put("src/main/resources/openapi/poof.yaml", false);
    expectedEntryNames.put("src/test/java/dev/pollito/poof/PoofApplicationTests.java", false);
    expectedEntryNames.put(".gitignore", false);
    expectedEntryNames.put("HELP.md", false);
    expectedEntryNames.put("mvnw", false);
    expectedEntryNames.put("mvnw.cmd", false);
    expectedEntryNames.put("pom.xml", false);

    if (request.getOptions().getLoggingAspect()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/aspect/LoggingAspect.java", false);
    }
    if (request.getOptions().getLogFilter()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/config/LogFilterConfig.java", false);
      expectedEntryNames.put("src/main/java/dev/pollito/poof/filter/LogFilter.java", false);
    }
    if (request.getOptions().getAllowCorsFromAnySource()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/config/WebConfig.java", false);
    }
    if (request.getOptions().getControllerAdvice()) {
      expectedEntryNames.put(
          "src/main/java/dev/pollito/poof/controller/advice/GlobalControllerAdvice.java", false);
    }
    return expectedEntryNames;
  }

  private void aspectAssertions(@NotNull GenerateRequest request, String aspectContent) {
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
    if (request.getOptions().getLoggingAspect()) {
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
