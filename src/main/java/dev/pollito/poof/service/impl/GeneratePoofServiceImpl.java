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
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class GeneratePoofServiceImpl implements GeneratePoofService {
  @Override
  public ByteArrayOutputStream generateFiles(GenerateRequest generateRequest) throws IOException {
    Resource baseTemplateResource = new ClassPathResource("baseTemplate");
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      File baseTemplateFolder = baseTemplateResource.getFile();
      zipFolder(baseTemplateFolder, "", zipOutputStream, generateRequest);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException("Error zipping baseTemplate folder", e);
    }

    return byteArrayOutputStream;
  }

  private void zipFolder(
      @NotNull File folder,
      String parentFolder,
      ZipOutputStream zipOutputStream,
      GenerateRequest generateRequest)
      throws IOException {
    File[] files = folder.listFiles();

    if (files != null) {
      for (File file : files) {
        String zipEntryName = parentFolder + file.getName();
        if (file.isDirectory()) {
          zipFolder(file, zipEntryName + "/", zipOutputStream, generateRequest);
        } else {
          if ("pom.xml".equals(file.getName())) {
            addPomFileToZip(
                file, zipEntryName, zipOutputStream, generateRequest.getProjectMetadata());
          } else if (file.getName().endsWith(".java")) {
            addJavaFileToZip(
                file, zipEntryName, zipOutputStream, generateRequest.getProjectMetadata());
          } else {
            addFileToZip(file, zipEntryName, zipOutputStream);
          }
        }
      }
    }
  }

  private void addJavaFileToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull ProjectMetadata projectMetadata)
      throws IOException {
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

  private void addPomFileToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull ProjectMetadata projectMetadata)
      throws IOException {
    String content = Files.readString(file.toPath());

    content = content.replace("<!--groupId-->", projectMetadata.getGroup());
    content = content.replace("<!--artifactId-->", projectMetadata.getArtifact());
    content = content.replace("<!--description-->", projectMetadata.getDescription());

    ZipEntry zipEntry = new ZipEntry(zipEntryName);
    zipOutputStream.putNextEntry(zipEntry);
    zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    zipOutputStream.closeEntry();
  }

  private void addFileToZip(
      File file, String zipEntryName, @NotNull ZipOutputStream zipOutputStream) throws IOException {
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

  private void writeToFileStream(
      @NotNull FileInputStream fileInputStream, ZipOutputStream zipOutputStream)
      throws IOException {
    byte[] buffer = new byte[1024];
    int length;
    while ((length = fileInputStream.read(buffer)) > 0) {
      zipOutputStream.write(buffer, 0, length);
    }
  }
}
