package dev.pollito.poof.controller;

import dev.pollito.poof.api.GenerateApi;
import dev.pollito.poof.model.Contracts;
import dev.pollito.poof.service.GenerateService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GenerateController implements GenerateApi {
  private final GenerateService generateService;

  @Override
  @SneakyThrows
  public ResponseEntity<Resource> generate(Contracts contracts) {

    // Create a ByteArrayOutputStream to hold the ZIP data
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

    // Add each generated file and any other files in the folder to the ZIP
    for (File file : generateService.generateFiles()) {
      // Add the parent folder and all its contents to the ZIP
      addFolderToZip(file.getParentFile(), zipOutputStream);
    }

    // Close the ZIP output stream
    zipOutputStream.close();

    // Prepare the InputStreamResource from the ByteArrayOutputStream
    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    // Set HTTP headers
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=files.zip");

    return ResponseEntity
            .ok()
            .headers(headers)
            .contentLength(byteArrayOutputStream.size())
            .body(resource);
  }

  private void addFolderToZip(File folder, ZipOutputStream zipOutputStream) {
    // List all files in the folder
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          // Recursively add sub-folders
          addFolderToZip(file, zipOutputStream);
        } else {
          // Add file to the ZIP with the correct relative path
          try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String zipEntryName = folder.getName() + "/" + file.getName();
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zipOutputStream.putNextEntry(zipEntry);

            // Write the file data to the ZIP
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
              zipOutputStream.write(buffer, 0, length);
            }

            zipOutputStream.closeEntry();
          } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error adding file to ZIP: " + file.getAbsolutePath());
          }
        }
      }
    }
  }
}
