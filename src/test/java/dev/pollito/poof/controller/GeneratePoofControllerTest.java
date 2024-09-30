package dev.pollito.poof.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.PoofRequest;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.GeneratePoofService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GeneratePoofControllerTest {
  @InjectMocks private GeneratePoofController generatePoofController;
  @Mock private GeneratePoofService generatePoofService;

  @Test
  @SneakyThrows
  void generateReturnsOk() {
    PoofRequest request =
        new PoofRequest()
            .projectMetadata(
                new ProjectMetadata()
                    .group("dev.pollito")
                    .artifact("poof")
                    .description("poof - Pollito Over Opinionated Framework"))
            .options(new Options().loggingAspect(true));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write("sample content".getBytes());

    HttpHeaders headers = new HttpHeaders();
    headers.add(
        "Content-Disposition",
        "attachment; filename=" + request.getProjectMetadata().getArtifact() + ".zip");

    when(generatePoofService.generateFiles(any(PoofRequest.class)))
        .thenReturn(byteArrayOutputStream);

    ResponseEntity<Resource> expectedResponse =
        ResponseEntity.ok()
            .headers(headers)
            .contentLength(byteArrayOutputStream.size())
            .body(
                new InputStreamResource(
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));

    ResponseEntity<Resource> actualResponse = generatePoofController.generate(request);
    assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    assertEquals(expectedResponse.getHeaders(), actualResponse.getHeaders());
    assertNotNull(actualResponse.getBody());
  }

  @Test
  void generateThrowsException() throws IOException {
    PoofRequest request = new PoofRequest();

    when(generatePoofService.generateFiles(any(PoofRequest.class)))
        .thenThrow(new IOException("Failed to generate files"));

    assertEquals(
        "Failed to generate files",
        assertThrows(IOException.class, () -> generatePoofController.generate(request))
            .getMessage());
  }
}
