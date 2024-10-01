package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.pollito.poof.model.Contract;
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

  private static final String PROJECT_METADATA_GROUP = "dev.pollito";
  private static final String PROJECT_METADATA_ARTIFACT = "poof";
  private static final String PROJECT_METADATA_DESCRIPTION =
      "poof - Pollito Over Opinionated Framework";

  public static final String PETSTORE = "petstore";
  private static final Map<String, String> BASE64_OAS_FILE =
      Map.of(
          PETSTORE,
          "b3BlbmFwaTogIjMuMC4wIgppbmZvOgogIHZlcnNpb246IDEuMC4wCiAgdGl0bGU6IFN3YWdnZXIgUGV0c3RvcmUKICBsaWNlbnNlOgogICAgbmFtZTogTUlUCnNlcnZlcnM6CiAgLSB1cmw6IGh0dHA6Ly9wZXRzdG9yZS5zd2FnZ2VyLmlvL3YxCnBhdGhzOgogIC9wZXRzOgogICAgZ2V0OgogICAgICBzdW1tYXJ5OiBMaXN0IGFsbCBwZXRzCiAgICAgIG9wZXJhdGlvbklkOiBsaXN0UGV0cwogICAgICB0YWdzOgogICAgICAgIC0gcGV0cwogICAgICBwYXJhbWV0ZXJzOgogICAgICAgIC0gbmFtZTogbGltaXQKICAgICAgICAgIGluOiBxdWVyeQogICAgICAgICAgZGVzY3JpcHRpb246IEhvdyBtYW55IGl0ZW1zIHRvIHJldHVybiBhdCBvbmUgdGltZSAobWF4IDEwMCkKICAgICAgICAgIHJlcXVpcmVkOiBmYWxzZQogICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICB0eXBlOiBpbnRlZ2VyCiAgICAgICAgICAgIG1heGltdW06IDEwMAogICAgICAgICAgICBmb3JtYXQ6IGludDMyCiAgICAgIHJlc3BvbnNlczoKICAgICAgICAnMjAwJzoKICAgICAgICAgIGRlc2NyaXB0aW9uOiBBIHBhZ2VkIGFycmF5IG9mIHBldHMKICAgICAgICAgIGhlYWRlcnM6CiAgICAgICAgICAgIHgtbmV4dDoKICAgICAgICAgICAgICBkZXNjcmlwdGlvbjogQSBsaW5rIHRvIHRoZSBuZXh0IHBhZ2Ugb2YgcmVzcG9uc2VzCiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgdHlwZTogc3RyaW5nCiAgICAgICAgICBjb250ZW50OgogICAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOiAgICAKICAgICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvUGV0cyIKICAgICAgICBkZWZhdWx0OgogICAgICAgICAgZGVzY3JpcHRpb246IHVuZXhwZWN0ZWQgZXJyb3IKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL0Vycm9yIgogICAgcG9zdDoKICAgICAgc3VtbWFyeTogQ3JlYXRlIGEgcGV0CiAgICAgIG9wZXJhdGlvbklkOiBjcmVhdGVQZXRzCiAgICAgIHRhZ3M6CiAgICAgICAgLSBwZXRzCiAgICAgIHJlcXVlc3RCb2R5OgogICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOgogICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL1BldCcKICAgICAgICByZXF1aXJlZDogdHJ1ZQogICAgICByZXNwb25zZXM6CiAgICAgICAgJzIwMSc6CiAgICAgICAgICBkZXNjcmlwdGlvbjogTnVsbCByZXNwb25zZQogICAgICAgIGRlZmF1bHQ6CiAgICAgICAgICBkZXNjcmlwdGlvbjogdW5leHBlY3RlZCBlcnJvcgogICAgICAgICAgY29udGVudDoKICAgICAgICAgICAgYXBwbGljYXRpb24vanNvbjoKICAgICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvRXJyb3IiCiAgL3BldHMve3BldElkfToKICAgIGdldDoKICAgICAgc3VtbWFyeTogSW5mbyBmb3IgYSBzcGVjaWZpYyBwZXQKICAgICAgb3BlcmF0aW9uSWQ6IHNob3dQZXRCeUlkCiAgICAgIHRhZ3M6CiAgICAgICAgLSBwZXRzCiAgICAgIHBhcmFtZXRlcnM6CiAgICAgICAgLSBuYW1lOiBwZXRJZAogICAgICAgICAgaW46IHBhdGgKICAgICAgICAgIHJlcXVpcmVkOiB0cnVlCiAgICAgICAgICBkZXNjcmlwdGlvbjogVGhlIGlkIG9mIHRoZSBwZXQgdG8gcmV0cmlldmUKICAgICAgICAgIHNjaGVtYToKICAgICAgICAgICAgdHlwZTogc3RyaW5nCiAgICAgIHJlc3BvbnNlczoKICAgICAgICAnMjAwJzoKICAgICAgICAgIGRlc2NyaXB0aW9uOiBFeHBlY3RlZCByZXNwb25zZSB0byBhIHZhbGlkIHJlcXVlc3QKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL1BldCIKICAgICAgICBkZWZhdWx0OgogICAgICAgICAgZGVzY3JpcHRpb246IHVuZXhwZWN0ZWQgZXJyb3IKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL0Vycm9yIgpjb21wb25lbnRzOgogIHNjaGVtYXM6CiAgICBQZXQ6CiAgICAgIHR5cGU6IG9iamVjdAogICAgICByZXF1aXJlZDoKICAgICAgICAtIGlkCiAgICAgICAgLSBuYW1lCiAgICAgIHByb3BlcnRpZXM6CiAgICAgICAgaWQ6CiAgICAgICAgICB0eXBlOiBpbnRlZ2VyCiAgICAgICAgICBmb3JtYXQ6IGludDY0CiAgICAgICAgbmFtZToKICAgICAgICAgIHR5cGU6IHN0cmluZwogICAgICAgIHRhZzoKICAgICAgICAgIHR5cGU6IHN0cmluZwogICAgUGV0czoKICAgICAgdHlwZTogYXJyYXkKICAgICAgbWF4SXRlbXM6IDEwMAogICAgICBpdGVtczoKICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvUGV0IgogICAgRXJyb3I6CiAgICAgIHR5cGU6IG9iamVjdAogICAgICByZXF1aXJlZDoKICAgICAgICAtIGNvZGUKICAgICAgICAtIG1lc3NhZ2UKICAgICAgcHJvcGVydGllczoKICAgICAgICBjb2RlOgogICAgICAgICAgdHlwZTogaW50ZWdlcgogICAgICAgICAgZm9ybWF0OiBpbnQzMgogICAgICAgIG1lc3NhZ2U6CiAgICAgICAgICB0eXBlOiBzdHJpbmcK");

  private static Stream<Options> optionsProvider() {
    List<Options> optionsList = new ArrayList<>();
    for (boolean allowCors : new boolean[] {true, false}) {
      for (boolean controllerAdvice : new boolean[] {true, false}) {
        for (boolean logFilter : new boolean[] {true, false}) {
          for (boolean loggingAspect : new boolean[] {true, false}) {
            for (boolean consumeOtherServices : new boolean[] {true, false}) {
              optionsList.add(
                  new Options()
                      .allowCorsFromAnySource(allowCors)
                      .controllerAdvice(controllerAdvice)
                      .logFilter(logFilter)
                      .loggingAspect(loggingAspect)
                      .consumeOtherServices(consumeOtherServices));
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
            .contract(new Contract().content(BASE64_OAS_FILE.get(PETSTORE)).name(PETSTORE))
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
    Map<String, Boolean> expectedEntryNames = new HashMap<>();
    expectedEntryNames.put(".mvn/wrapper/maven-wrapper.properties", false);
    expectedEntryNames.put("src/main/java/dev/pollito/poof/PoofApplication.java", false);
    expectedEntryNames.put("src/main/resources/application.yml", false);
    expectedEntryNames.put("src/main/resources/openapi/" + PETSTORE + ".yaml", false);
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
