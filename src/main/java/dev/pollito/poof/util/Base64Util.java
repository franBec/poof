package dev.pollito.poof.util;

import dev.pollito.poof.exception.InvalidServerUrlException;
import dev.pollito.poof.model.GenerateRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

public class Base64Util {
  private Base64Util() {}

  @SneakyThrows
  public static void addBase64FileToZip(
      @NotNull GenerateRequest generateRequest, ZipOutputStream zipOutputStream) {
    String base64File = generateRequest.getContracts().getProviderContract().getContent();
    if (Objects.nonNull(base64File) && !base64File.isEmpty()) {
      try (ByteArrayInputStream inputStream =
          new ByteArrayInputStream(Base64.getDecoder().decode(base64File))) {
        zipOutputStream.putNextEntry(
            new ZipEntry(
                "src/main/resources/openapi/"
                    + generateRequest.getContracts().getProviderContract().getName()
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

  public static @NotNull String getServerUrlFromBase64(String base64EncodedYaml) {
    byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedYaml);
    String yamlContent = new String(decodedBytes, StandardCharsets.UTF_8);

    Yaml yaml = new Yaml();
    Map<String, Object> yamlMap = yaml.load(yamlContent);

    if (yamlMap.containsKey("servers")) {
      List<Map<String, String>> serversList = (List<Map<String, String>>) yamlMap.get("servers");

      if (!serversList.isEmpty() && serversList.get(0).containsKey("url")) {
        String url = serversList.get(0).get("url");

        return validateAndTransformUrl(url);
      }
    }

    throw new InvalidServerUrlException();
  }

  private static @NotNull String validateAndTransformUrl(String url) {
    String urlPattern = "^(https?://)?([a-zA-Z0-9-]+\\.[a-zA-Z]{2,})(/[\\S]*)?$";
    Pattern pattern = Pattern.compile(urlPattern);
    Matcher matcher = pattern.matcher(url);

    if (matcher.matches()) {
      String domain = matcher.group(2);

      String[] parts = domain.split("\\.");
      StringBuilder packageName = new StringBuilder();

      for (int i = parts.length - 1; i >= 0; i--) {
        packageName.append(parts[i]);
        if (i != 0) {
          packageName.append(".");
        }
      }

      return packageName.toString();
    }

    throw new InvalidServerUrlException();
  }
}
