package dev.pollito.poof.service.impl;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.service.GenerateService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class GenerateServiceImpl implements GenerateService {
  @Override
  public ByteArrayOutputStream generateFiles(GenerateRequest generateRequest) throws IOException {
    Resource baseTemplateResource = new ClassPathResource("baseTemplate");

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      addFolderToZip(baseTemplateResource.getFile(), "", zipOutputStream);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException("Error zipping baseTemplate folder", e);
    }

    return byteArrayOutputStream;
  }

  private void addFolderToZip(
      @NotNull File folder, String parentFolder, ZipOutputStream zipOutputStream)
      throws IOException {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        String zipEntryName = parentFolder + file.getName();
        if (file.isDirectory()) {
          addFolderToZip(file, zipEntryName + "/", zipOutputStream);
        } else {
          try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zipOutputStream.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
              zipOutputStream.write(buffer, 0, length);
            }

            zipOutputStream.closeEntry();
          } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error adding file to ZIP: " + file.getAbsolutePath(), e);
          }
        }
      }
    }
  }
}
