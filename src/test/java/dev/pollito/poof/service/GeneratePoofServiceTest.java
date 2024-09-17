package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.impl.GeneratePoofServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.SneakyThrows;
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
    ByteArrayOutputStream zipOutputStream =
        generatePoofService.generateFiles(
            new GenerateRequest()
                .projectMetadata(
                    new ProjectMetadata()
                        .group(PROJECT_METADATA_GROUP)
                        .artifact(PROJECT_METADATA_ARTIFACT)
                        .description(PROJECT_METADATA_DESCRIPTION))
                .options(new Options().loggingAspect(true)));

    try (ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(zipOutputStream.toByteArray()))) {
      boolean pomXmlExists = false;
      boolean appJavaExists = false;
      boolean appTestsJavaExists = false;
      boolean applicationYmlExists = false;

      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        String entryName = entry.getName();

        if (entryName.equals("pom.xml")) {
          pomXmlExists = true;
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
      }

      // Assert that the expected files exist in the ZIP
      assertTrue(pomXmlExists);
      assertTrue(appJavaExists);
      assertTrue(appTestsJavaExists);
      assertTrue(applicationYmlExists);
    }
  }
}
