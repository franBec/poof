package dev.pollito.poof.service.impl;

import dev.pollito.poof.service.GenerateService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class GenerateServiceImpl implements GenerateService {
  @Override
  public ByteArrayOutputStream generateFiles() throws IOException {
    // Locate the baseTemplate folder in the resources directory
    Resource baseTemplateResource = new ClassPathResource("baseTemplate");

    // Prepare a ByteArrayOutputStream to hold the ZIP data
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      // Recursively add files and folders to the ZIP
      addFolderToZip(baseTemplateResource.getFile(), "", zipOutputStream);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException("Error zipping baseTemplate folder", e);
    }

    return byteArrayOutputStream;
  }

  // Recursive helper method to add files and directories to the ZIP output stream
  private void addFolderToZip(File folder, String parentFolder, ZipOutputStream zipOutputStream)
      throws IOException {
    // Get all files and subdirectories in the current folder
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        String zipEntryName = parentFolder + file.getName();
        if (file.isDirectory()) {
          // Recursively add subfolders, appending "/" to denote a folder in the ZIP
          addFolderToZip(file, zipEntryName + "/", zipOutputStream);
        } else {
          // Add a file to the ZIP
          try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zipOutputStream.putNextEntry(zipEntry);

            // Write file data to the ZIP
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
