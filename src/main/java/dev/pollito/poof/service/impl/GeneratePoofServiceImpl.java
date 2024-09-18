package dev.pollito.poof.service.impl;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.GeneratePoofService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class GeneratePoofServiceImpl implements GeneratePoofService {

  public static final String SRC_MAIN_JAVA_COM_EXAMPLE_DEMO = "src/main/java/com/example/demo";

  @Override
  @SneakyThrows
  public ByteArrayOutputStream generateFiles(GenerateRequest generateRequest) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    File baseFolder = new ClassPathResource("baseTemplate").getFile();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      zipFolder(baseFolder, "", zipOutputStream, generateRequest);
      addBase64FileToZip(generateRequest, zipOutputStream);
    }

    return byteArrayOutputStream;
  }

  @SneakyThrows
  private static void addBase64FileToZip(
      @NotNull GenerateRequest generateRequest, ZipOutputStream zipOutputStream) {
    String base64File = generateRequest.getContracts().getProviderContract();
    if (base64File != null && !base64File.isEmpty()) {
      try (ByteArrayInputStream inputStream =
          new ByteArrayInputStream(Base64.getDecoder().decode(base64File))) {
        zipOutputStream.putNextEntry(
            new ZipEntry(
                "src/main/resources/openapi/"
                    + generateRequest.getProjectMetadata().getArtifact()
                    + ".yaml"));
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
          zipOutputStream.write(buffer, 0, len);
        }
        zipOutputStream.closeEntry();
      }
    }
  }

  @SneakyThrows
  private static void zipFolder(
      @NotNull File folder,
      String parentFolder,
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest) {
    File[] files = folder.listFiles();

    if (files != null) {
      for (File file : files) {
        String zipEntryName =
            getNewZipEntryName(parentFolder, file, generateRequest.getProjectMetadata());

        if (file.isDirectory()) {
          addFolderToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else if ("pom.xml".equals(file.getName())) {
          addPomXmlToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else if ("application.yml".equals(file.getName())) {
          addApplicationYmlToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else if (file.getName().endsWith(".java")) {
          addJavaFileToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else {
          addFileToZip(file, zipEntryName, zipOutputStream);
        }
      }
    }
  }

  private static void addFolderToZip(
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    zipFolder(file, zipEntryName + "/", zipOutputStream, generateRequest);
  }

  private static void addJavaFileToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      @NotNull String zipEntryName) {
    List<Entry<String, Boolean>> conditions =
        Arrays.asList(
            new SimpleEntry<>(
                "aspect/LoggingAspect.java", generateRequest.getOptions().getLoggingAspect()),
            new SimpleEntry<>(
                "config/WebConfig.java", generateRequest.getOptions().getAllowCorsFromAnySource()),
            new SimpleEntry<>(
                "controller/advice/GlobalControllerAdvice.java",
                generateRequest.getOptions().getControllerAdvice()),
            new SimpleEntry<>(
                "config/LogFilterConfig.java", generateRequest.getOptions().getLogFilter()),
            new SimpleEntry<>(
                "filter/LogFilter.java", generateRequest.getOptions().getLogFilter()));

    for (Entry<String, Boolean> condition : conditions) {
      if (skipFile(zipEntryName, condition.getKey(), condition.getValue())) {
        return;
      }
    }
    addFileWithReplacementsToZip(
        file,
        zipEntryName,
        zipOutputStream,
        javaReplacements(generateRequest.getProjectMetadata()));
  }

  private static boolean skipFile(
      @NotNull String zipEntryName, String suffix, Boolean generateRequest) {
    return zipEntryName.endsWith(suffix) && !generateRequest;
  }

  private static void addApplicationYmlToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    addFileWithReplacementsToZip(
        file,
        zipEntryName,
        zipOutputStream,
        applicationYmlReplacements(generateRequest.getProjectMetadata()));
  }

  private static void addPomXmlToZip(
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    addFileWithReplacementsToZip(
        file, zipEntryName, zipOutputStream, pomXmlReplacements(generateRequest));
  }

  private static @NotNull Map<String, String> applicationYmlReplacements(
      @NotNull ProjectMetadata projectMetadata) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("#artifact", projectMetadata.getArtifact());

    return replacements;
  }

  private static @NotNull Map<String, String> javaReplacements(
      @NotNull ProjectMetadata projectMetadata) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("/*group*/", projectMetadata.getGroup());
    replacements.put("/*artifact*/", projectMetadata.getArtifact());
    replacements.put("/*Artifact*/", capitalizeFirstLetter(projectMetadata.getArtifact()));

    return replacements;
  }

  private static @NotNull Map<String, String> pomXmlReplacements(
      @NotNull GenerateRequest generateRequest) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("<!--groupId-->", generateRequest.getProjectMetadata().getGroup());
    replacements.put("<!--artifactId-->", generateRequest.getProjectMetadata().getArtifact());
    replacements.put("<!--description-->", generateRequest.getProjectMetadata().getDescription());
    if (Boolean.FALSE.equals(generateRequest.getOptions().getLoggingAspect())) {
      replacements.put(
          "\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.aspectj</groupId>\r\n\t\t\t<artifactId>aspectjtools</artifactId>\r\n\t\t\t<version>1.9.22.1</version>\r\n\t\t</dependency>",
          "");
    }

    return replacements;
  }

  private static @NotNull String getNewZipEntryName(
      @NotNull String parentFolder, File file, @NotNull ProjectMetadata projectMetadata) {
    String groupPath = projectMetadata.getGroup().replace('.', '/');
    String artifact = projectMetadata.getArtifact();
    String zipEntryName;

    if (parentFolder.startsWith(SRC_MAIN_JAVA_COM_EXAMPLE_DEMO)) {
      zipEntryName =
          parentFolder.replace(
                  SRC_MAIN_JAVA_COM_EXAMPLE_DEMO, "src/main/java/" + groupPath + "/" + artifact)
              + file.getName();
    } else if (parentFolder.startsWith("src/test/java/com/example/demo")) {
      zipEntryName =
          parentFolder.replace(
                  "src/test/java/com/example/demo", "src/test/java/" + groupPath + "/" + artifact)
              + file.getName();
    } else {
      zipEntryName = parentFolder + file.getName();
    }

    if ("DemoApplication.java".equals(file.getName())) {
      zipEntryName =
          zipEntryName.replace(
              "DemoApplication.java", capitalizeFirstLetter(artifact) + "Application.java");
    } else if ("DemoApplicationTests.java".equals(file.getName())) {
      zipEntryName =
          zipEntryName.replace(
              "DemoApplicationTests.java",
              capitalizeFirstLetter(artifact) + "ApplicationTests.java");
    }

    return zipEntryName;
  }

  @SneakyThrows
  private static void addFileWithReplacementsToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull Map<String, String> replacements) {
    String content = Files.readString(file.toPath());

    for (Entry<String, String> entry : replacements.entrySet()) {
      content = content.replace(entry.getKey(), entry.getValue());
    }

    zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
    zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    zipOutputStream.closeEntry();
  }

  @SneakyThrows
  private static void addFileToZip(
      File file, String zipEntryName, @NotNull ZipOutputStream zipOutputStream) {
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      ZipEntry zipEntry = new ZipEntry(zipEntryName);
      zipOutputStream.putNextEntry(zipEntry);

      writeToFileStream(fileInputStream, zipOutputStream);
      zipOutputStream.closeEntry();
    }
  }

  @SneakyThrows
  private static void writeToFileStream(
      @NotNull FileInputStream fileInputStream, ZipOutputStream zipOutputStream) {
    byte[] buffer = new byte[1024];
    int length;
    while ((length = fileInputStream.read(buffer)) > 0) {
      zipOutputStream.write(buffer, 0, length);
    }
  }
}
