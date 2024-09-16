package dev.pollito.poof.controller;

import dev.pollito.poof.api.GenerateApi;
import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.service.GeneratePoofService;
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
public class GeneratePoofController implements GenerateApi {
  private final GeneratePoofService generatePoofService;

  @Override
  @SneakyThrows
  public ResponseEntity<Resource> generate(GenerateRequest generateRequest) {
    ByteArrayOutputStream byteArrayOutputStream = generatePoofService.generateFiles(generateRequest);

    InputStreamResource resource =
        new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=baseTemplate.zip");

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(byteArrayOutputStream.size())
        .body(resource);
  }
}
