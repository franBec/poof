package dev.pollito.poof.service.impl;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.GeneratePoofService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
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

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      zipFolder(
          new ClassPathResource("baseTemplate").getFile(), "", zipOutputStream, generateRequest);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException("Error zipping baseTemplate folder", e);
    }

    return byteArrayOutputStream;
  }

  @SneakyThrows
  private void zipFolder(
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
          addFolderToZip(parentFolder, zipOutputStream, generateRequest, file, zipEntryName);
        } else if ("pom.xml".equals(file.getName())) {
          addPomXmlToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else if ("application.yml".equals(file.getName())) {
          addApplicationYmlToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else if (file.getName().endsWith(".java")) {
          addJavaFileToZip(zipOutputStream, generateRequest, file, zipEntryName);
        } else{
          addFileToZip(file, zipEntryName, zipOutputStream);
        }
      }
    }
  }

  private void addFolderToZip(
      String parentFolder,
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    if (isAspectFolder(file, parentFolder)
        && Boolean.FALSE.equals(generateRequest.getOptions().getLoggingAspect())) {
      return;
    }
    zipFolder(file, zipEntryName + "/", zipOutputStream, generateRequest);
  }

  private void addJavaFileToZip(
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    addFileWithReplacementsToZip(
        file,
        zipEntryName,
        zipOutputStream,
        javaReplacements(generateRequest.getProjectMetadata()));
  }

  private void addApplicationYmlToZip(
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    addFileWithReplacementsToZip(
        file,
        zipEntryName,
        zipOutputStream,
        applicationYmlReplacements(generateRequest.getProjectMetadata()));
  }

  private void addPomXmlToZip(
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest,
      File file,
      String zipEntryName) {
    addFileWithReplacementsToZip(
        file, zipEntryName, zipOutputStream, pomXmlReplacements(generateRequest));
  }

  private boolean isAspectFolder(@NotNull File file, String parentFolder) {
    return "aspect".equals(file.getName())
        && parentFolder.startsWith(SRC_MAIN_JAVA_COM_EXAMPLE_DEMO);
  }

  private @NotNull Map<String, String> applicationYmlReplacements(
      @NotNull ProjectMetadata projectMetadata) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("#artifact", projectMetadata.getArtifact());

    return replacements;
  }

  private @NotNull Map<String, String> javaReplacements(@NotNull ProjectMetadata projectMetadata) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("/*group*/", projectMetadata.getGroup());
    replacements.put("/*artifact*/", projectMetadata.getArtifact());
    replacements.put("/*Artifact*/", capitalizeFirstLetter(projectMetadata.getArtifact()));

    return replacements;
  }

  private @NotNull Map<String, String> pomXmlReplacements(
      @NotNull GenerateRequest generateRequest) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("<!--groupId-->", generateRequest.getProjectMetadata().getGroup());
    replacements.put("<!--artifactId-->", generateRequest.getProjectMetadata().getArtifact());
    replacements.put("<!--description-->", generateRequest.getProjectMetadata().getDescription());
    if (Boolean.FALSE.equals(generateRequest.getOptions().getLoggingAspect())) {
      replacements.put(
          "\n\t\t<dependency>\n\t\t\t<groupId>org.aspectj</groupId>\n\t\t\t<artifactId>aspectjtools</artifactId>\n\t\t\t<version>1.9.22.1</version>\n\t\t</dependency>",
          "");
    }

    return replacements;
  }

  private @NotNull String getNewZipEntryName(
      @NotNull String parentFolder, File file, @NotNull ProjectMetadata projectMetadata) {
    String groupPath = projectMetadata.getGroup().replace('.', '/');
    String artifact = projectMetadata.getArtifact();

    if (parentFolder.startsWith(SRC_MAIN_JAVA_COM_EXAMPLE_DEMO)) {
      return parentFolder.replace(
              SRC_MAIN_JAVA_COM_EXAMPLE_DEMO, "src/main/java/" + groupPath + "/" + artifact)
          + file.getName();
    }
    if (parentFolder.startsWith("src/test/java/com/example/demo")) {
      return parentFolder.replace(
              "src/test/java/com/example/demo", "src/test/java/" + groupPath + "/" + artifact)
          + file.getName();
    }
    return parentFolder + file.getName();
  }

  @SneakyThrows
  private void addFileWithReplacementsToZip(
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
  private void addFileToZip(
      File file, String zipEntryName, @NotNull ZipOutputStream zipOutputStream) {
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      ZipEntry zipEntry = new ZipEntry(zipEntryName);
      zipOutputStream.putNextEntry(zipEntry);

      writeToFileStream(fileInputStream, zipOutputStream);
      zipOutputStream.closeEntry();
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException("Error adding file to ZIP: " + file.getAbsolutePath(), e);
    }
  }

  @SneakyThrows
  private void writeToFileStream(
      @NotNull FileInputStream fileInputStream, ZipOutputStream zipOutputStream) {
    byte[] buffer = new byte[1024];
    int length;
    while ((length = fileInputStream.read(buffer)) > 0) {
      zipOutputStream.write(buffer, 0, length);
    }
  }
}
