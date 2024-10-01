package dev.pollito.poof.service;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.PoofRequest;
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

  public static final String PROJECT_METADATA_GROUP = "dev.pollito";
  public static final String PROJECT_METADATA_ARTIFACT = "post";
  public static final String PROJECT_METADATA_DESCRIPTION =
      "post - Pollito Opinionated Spring-Boot Template";

  private static Stream<Options> optionsProvider() {
    List<Options> optionsList = new ArrayList<>();
    for (boolean allowCors : new boolean[] {true, false}) {
      for (boolean controllerAdvice : new boolean[] {true, false}) {
        for (boolean logFilter : new boolean[] {true, false}) {
          for (boolean loggingAspect : new boolean[] {true, false}) {
            for (boolean consumesOtherServicesWithOAS : new boolean[] {true, false}) {
              optionsList.add(
                  new Options()
                      .allowCorsFromAnySource(allowCors)
                      .controllerAdvice(controllerAdvice)
                      .logFilter(logFilter)
                      .loggingAspect(loggingAspect)
                      .consumesOtherServicesWithOAS(consumesOtherServicesWithOAS));
            }
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
    PoofRequest request =
        new PoofRequest()
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
        FileAssertions.checkFileIsExpected(expectedEntryNames, entryName);

        if (entryName.equals("pom.xml")) {
          PomXmlAssertions.pomXmlAssertions(request, readZipEntryContent(zipInputStream));
        } else if (entryName.equals("src/main/resources/application.yml")) {
          ApplicationYmlAssertions.applicationYmlAssertions(readZipEntryContent(zipInputStream));
        } else if (entryName.endsWith(".java")) {
          JavaFilesAssertions.javaFilesAssertions(
              request, entryName, readZipEntryContent(zipInputStream));
        } else {
          FileAssertions.checkFileIsNotEmpty(entryName, readZipEntryContent(zipInputStream));
        }
        expectedEntryNames.put(entryName, true);
        zipInputStream.closeEntry();
      }

      FileAssertions.checkAllExpectedFilesWereCopied(expectedEntryNames);
    }
  }

  @NotNull
  private static Map<String, Boolean> buildExpectedEntryNamesMap(@NotNull PoofRequest request) {
    String groupArtifactPath =
        PROJECT_METADATA_GROUP.replace(".", "/") + "/" + PROJECT_METADATA_ARTIFACT;

    Map<String, Boolean> expectedEntryNames = new HashMap<>();
    expectedEntryNames.put(".mvn/wrapper/maven-wrapper.properties", false);
    expectedEntryNames.put(
        "src/main/java/"
            + groupArtifactPath
            + "/"
            + capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT)
            + "Application.java",
        false);
    expectedEntryNames.put(
        "src/main/java/"
            + groupArtifactPath
            + "/controller/"
            + capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT)
            + "Controller.java",
        false);
    expectedEntryNames.put("src/main/resources/application.yml", false);
    expectedEntryNames.put(
        "src/test/java/"
            + groupArtifactPath
            + "/"
            + capitalizeFirstLetter(PROJECT_METADATA_ARTIFACT)
            + "ApplicationTests.java",
        false);
    expectedEntryNames.put(".gitignore", false);
    expectedEntryNames.put("HELP.md", false);
    expectedEntryNames.put("mvnw", false);
    expectedEntryNames.put("mvnw.cmd", false);
    expectedEntryNames.put("pom.xml", false);

    if (request.getOptions().getLoggingAspect()) {
      expectedEntryNames.put(
          "src/main/java/" + groupArtifactPath + "/aspect/LoggingAspect.java", false);
    }
    if (request.getOptions().getLogFilter()) {
      expectedEntryNames.put(
          "src/main/java/" + groupArtifactPath + "/config/LogFilterConfig.java", false);
      expectedEntryNames.put(
          "src/main/java/" + groupArtifactPath + "/filter/LogFilter.java", false);
    }
    if (request.getOptions().getAllowCorsFromAnySource()) {
      expectedEntryNames.put(
          "src/main/java/" + groupArtifactPath + "/config/WebConfig.java", false);
    }
    if (request.getOptions().getControllerAdvice()) {
      expectedEntryNames.put(
          "src/main/java/" + groupArtifactPath + "/controller/advice/GlobalControllerAdvice.java",
          false);
    }
    return expectedEntryNames;
  }

  @SneakyThrows
  private static String readZipEntryContent(@NotNull InputStream inputStream) {
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
    PoofRequest request = new PoofRequest();
    assertThrows(RuntimeException.class, () -> generatePoofService.generateFiles(request));
  }
}
