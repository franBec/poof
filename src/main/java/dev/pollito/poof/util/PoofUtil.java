package dev.pollito.poof.util;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;
import static dev.pollito.poof.util.DependencyUtil.CONSUMER_EXECUTION_TEMPLATE;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.ProjectMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class PoofUtil {
  private PoofUtil() {}

  public static final String SRC_MAIN_JAVA_COM_EXAMPLE_DEMO = "src/main/java/com/example/demo";

  @SneakyThrows
  public static void zipFolder(
      @NotNull File folder,
      String parentFolder,
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest) {
    File[] files = folder.listFiles();

    if (Objects.nonNull(files)) {
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
    List<Map.Entry<String, Boolean>> conditions =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>(
                "aspect/LoggingAspect.java", generateRequest.getOptions().getLoggingAspect()),
            new AbstractMap.SimpleEntry<>(
                "config/WebConfig.java", generateRequest.getOptions().getAllowCorsFromAnySource()),
            new AbstractMap.SimpleEntry<>(
                "controller/advice/GlobalControllerAdvice.java",
                generateRequest.getOptions().getControllerAdvice()),
            new AbstractMap.SimpleEntry<>(
                "config/LogFilterConfig.java", generateRequest.getOptions().getLogFilter()),
            new AbstractMap.SimpleEntry<>(
                "filter/LogFilter.java", generateRequest.getOptions().getLogFilter()));

    for (Map.Entry<String, Boolean> condition : conditions) {
      if (skipFile(zipEntryName, condition.getKey(), condition.getValue())) {
        return;
      }
    }
    addFileToZip(
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
    addFileToZip(
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
    addFileToZip(file, zipEntryName, zipOutputStream, pomXmlReplacements(generateRequest, file));
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
      @NotNull GenerateRequest generateRequest, File pomXml) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("<!--groupId-->", generateRequest.getProjectMetadata().getGroup());
    replacements.put("<!--artifactId-->", generateRequest.getProjectMetadata().getArtifact());
    replacements.put("<!--description-->", generateRequest.getProjectMetadata().getDescription());
    if (Boolean.FALSE.equals(generateRequest.getOptions().getLoggingAspect())) {
      replacements.put(extractTextBetweenMarkers(pomXml, "<!--aspectj-->"), "");
    }
    if (generateRequest.getContracts().getConsumerContracts().isEmpty()) {
      replacements.put(extractTextBetweenMarkers(pomXml, "<!--consumer dependencies-->"), "");
    }
    replacements.put("<!--consumer generation-->", consumerGenerationReplacement(generateRequest));

    return replacements;
  }

  private static @NotNull String consumerGenerationReplacement(@NotNull GenerateRequest generateRequest) {
    if(generateRequest.getContracts().getConsumerContracts().isEmpty()){
      return "";
    }

    String name = "sample name";
    String uri = "com.example";

    return CONSUMER_EXECUTION_TEMPLATE
            .replace("<!--name-->",name)
            .replace("<!--apiPackage-->", uri+".api")
            .replace("<!--modelPackage-->", uri+".models");
  }

  @SneakyThrows
  private static @NotNull String extractTextBetweenMarkers(
      @NotNull File file, @NotNull String marker) {
    String fileContent = Files.readString(file.toPath());

    int markerLength = marker.length();
    int startIndex = fileContent.indexOf(marker);
    int endIndex = fileContent.indexOf(marker, startIndex + markerLength);

    return fileContent.substring(startIndex, endIndex + markerLength).trim();
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
  private static void addFileToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull Map<String, String> replacements) {
    String content = Files.readString(file.toPath());

    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      content = content.replace(entry.getKey(), entry.getValue());
    }

    zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
    zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    zipOutputStream.closeEntry();
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
