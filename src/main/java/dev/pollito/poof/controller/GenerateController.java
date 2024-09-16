package dev.pollito.poof.controller;

import dev.pollito.poof.api.GenerateApi;
import dev.pollito.poof.model.Contracts;
import dev.pollito.poof.service.GenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequiredArgsConstructor
public class GenerateController implements GenerateApi {
    private final GenerateService generateService;
    @Override
    public ResponseEntity<Resource> generate(Contracts contracts) throws IOException {

        // Create a ByteArrayOutputStream to hold the ZIP data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        // Add each generated file to the ZIP, preserving the folder structure
        for (File file : generateService.generateFiles()) {
            // Create a ZipEntry with the folder structure
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(file.getParentFile().getName() + "/" + file.getName());
                zipOutputStream.putNextEntry(zipEntry);

                // Write the file data to the ZIP
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }

                zipOutputStream.closeEntry();
            }
        }

        // Close the ZIP output stream
        zipOutputStream.close();

        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=files.zip");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(byteArrayOutputStream.size())
                .body(new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));

    }
}
