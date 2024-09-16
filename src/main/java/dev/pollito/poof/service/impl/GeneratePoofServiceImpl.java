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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class GeneratePoofServiceImpl implements GeneratePoofService {
  @Override
  @SneakyThrows
  public ByteArrayOutputStream generateFiles(GenerateRequest generateRequest) {
    Resource baseTemplateResource = new ClassPathResource("baseTemplate");
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      File baseTemplateFolder = baseTemplateResource.getFile();
      zipFolder(baseTemplateFolder, "", zipOutputStream, generateRequest.getProjectMetadata());
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
      ProjectMetadata projectMetadata) {
    File[] files = folder.listFiles();

    if (files != null) {
      for (File file : files) {
        String zipEntryName = getNewZipEntryName(parentFolder, file, projectMetadata);
        if (file.isDirectory()) {
          zipFolder(file, zipEntryName + "/", zipOutputStream, projectMetadata);
        } else {
          if ("pom.xml".equals(file.getName())) {
            addPomFileToZip(file, zipEntryName, zipOutputStream, projectMetadata);
          } else if (file.getName().endsWith(".java")) {
            addJavaFileToZip(file, zipEntryName, zipOutputStream, projectMetadata);
          } else {
            addFileToZip(file, zipEntryName, zipOutputStream);
          }
        }
      }
    }
  }

  private @NotNull String getNewZipEntryName(
      @NotNull String parentFolder, File file, @NotNull ProjectMetadata projectMetadata) {
    String groupPath = projectMetadata.getGroup().replace('.', '/');
    String artifact = projectMetadata.getArtifact();

    if (parentFolder.startsWith("src/main/java/com/example/demo")) {
      return parentFolder.replace(
              "src/main/java/com/example/demo", "src/main/java/" + groupPath + "/" + artifact)
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
  private void addJavaFileToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull ProjectMetadata projectMetadata) {
    String content = Files.readString(file.toPath());

    content = content.replace("/*group*/", projectMetadata.getGroup());
    content = content.replace("/*artifact*/", projectMetadata.getArtifact());
    String capitalizedArtifact = capitalizeFirstLetter(projectMetadata.getArtifact());
    content = content.replace("/*Artifact*/", capitalizedArtifact);

    ZipEntry zipEntry = new ZipEntry(zipEntryName);
    zipOutputStream.putNextEntry(zipEntry);
    zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    zipOutputStream.closeEntry();
  }

  @SneakyThrows
  private void addPomFileToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull ProjectMetadata projectMetadata) {
    String content = Files.readString(file.toPath());

    content = content.replace("<!--groupId-->", projectMetadata.getGroup());
    content = content.replace("<!--artifactId-->", projectMetadata.getArtifact());
    content = content.replace("<!--description-->", projectMetadata.getDescription());

    ZipEntry zipEntry = new ZipEntry(zipEntryName);
    zipOutputStream.putNextEntry(zipEntry);
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
