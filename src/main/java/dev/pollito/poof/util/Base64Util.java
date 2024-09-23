package dev.pollito.poof.util;

import dev.pollito.poof.model.GenerateRequest;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Base64Util {
    private Base64Util(){}

    @SneakyThrows
    public static void addBase64FileToZip(
            @NotNull GenerateRequest generateRequest, ZipOutputStream zipOutputStream) {
      String base64File = generateRequest.getContracts().getProviderContract();
      if (Objects.nonNull(base64File) && !base64File.isEmpty()) {
        try (ByteArrayInputStream inputStream =
            new ByteArrayInputStream(Base64.getDecoder().decode(base64File))) {
          zipOutputStream.putNextEntry(
              new ZipEntry(
                  "src/main/resources/openapi/"
                      + generateRequest.getProjectMetadata().getArtifact()
                      + ".yaml"));
          byte[] buffer = new byte[1024];
          int len;
          while ((len = inputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, len);
          }
          zipOutputStream.closeEntry();
        }
      }
    }
}
