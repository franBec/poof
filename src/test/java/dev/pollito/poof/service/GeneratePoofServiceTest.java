package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.impl.GeneratePoofServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    ByteArrayOutputStream zipOutputStream = generatePoofService.generateFiles(request);

    try (ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(zipOutputStream.toByteArray()))) {
      boolean pomXmlExists = false;
      boolean appJavaExists = false;
      boolean appTestsJavaExists = false;
      boolean applicationYmlExists = false;
      String pomXmlContent = null;

      ZipEntry entry;
      while (Objects.nonNull(entry = zipInputStream.getNextEntry())) {
        String entryName = entry.getName();

        if (entryName.equals("pom.xml")) {
          pomXmlExists = true;
          pomXmlContent = readZipEntryContent(zipInputStream);
        }
        if (entryName.equals("src/main/java/dev/pollito/poof/PoofApplication.java")) {
          appJavaExists = true;
        }
        if (entryName.equals("src/test/java/dev/pollito/poof/PoofApplicationTests.java")) {
          appTestsJavaExists = true;
        }
        if (entryName.equals("src/main/resources/application.yml")) {
          applicationYmlExists = true;
        }
        zipInputStream.closeEntry();
      }

      pomXmlAssertions(request, pomXmlExists, pomXmlContent);

      assertTrue(appJavaExists, "PoofApplication.java file should exist");
      assertTrue(appTestsJavaExists, "PoofApplicationTests.java file should exist");
      assertTrue(applicationYmlExists, "application.yml file should exist");
    }
  }

  private void pomXmlAssertions(
      @NotNull GenerateRequest request, boolean pomXmlExists, String pomXmlContent) {
    assertTrue(pomXmlExists, "pom.xml file should exist");
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
    if (Boolean.TRUE.equals(request.getOptions().getLoggingAspect())) {
      assertTrue(
          pomXmlContent.contains(
              "\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.aspectj</groupId>\r\n\t\t\t<artifactId>aspectjtools</artifactId>\r\n\t\t\t<version>1.9.22.1</version>\r\n\t\t</dependency>"),
          "pom.xml should contain aspect dependency");
    } else {
      assertFalse(
          pomXmlContent.contains(
              "\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.aspectj</groupId>\r\n\t\t\t<artifactId>aspectjtools</artifactId>\r\n\t\t\t<version>1.9.22.1</version>\r\n\t\t</dependency>"),
          "pom.xml should not contain aspect dependency");
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
}
