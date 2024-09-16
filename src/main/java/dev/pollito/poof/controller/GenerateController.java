package dev.pollito.poof.controller;

import dev.pollito.poof.api.GenerateApi;
import dev.pollito.poof.model.Contracts;
import dev.pollito.poof.service.GenerateService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    // Generate ZIP of baseTemplate folder
    ByteArrayOutputStream zipOutputStream = generateService.generateFiles();

    // Prepare InputStreamResource from ByteArrayOutputStream
    InputStreamResource resource =
        new InputStreamResource(new ByteArrayInputStream(zipOutputStream.toByteArray()));

    // Set HTTP headers for downloading the file
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=baseTemplate.zip");

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(zipOutputStream.size())
        .body(resource);
  }
}
