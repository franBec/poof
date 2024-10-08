package dev.pollito.poof.util;

import dev.pollito.poof.model.Contract;
import dev.pollito.poof.model.PoofRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class ApplicationYmlUtil {
  private ApplicationYmlUtil() {}

  private static final String CLIENT_URL_DEFINITION =
      """

                  /*client*/:
                    baseUrl: localhost:8080/replace-this-url #replace this url
                  """;

  public static void addFileToZip(
      ZipOutputStream zipOutputStream, @NotNull PoofRequest request, File file, String zipEntryName)
      throws IOException {
    ZipUtil.addFileToZip(file, zipEntryName, zipOutputStream, applicationYmlReplacements(request));
  }

  private static @NotNull Map<String, String> applicationYmlReplacements(
      @NotNull PoofRequest request) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("#artifact", request.getProjectMetadata().getArtifact());
    replacements.put(
        "#clienturls", clientUrlsReplacement(request.getContracts().getConsumerContracts()));
    return replacements;
  }

  private static @NotNull String clientUrlsReplacement(@NotNull List<Contract> clientContracts) {
    if (clientContracts.isEmpty()) {
      return "";
    }
    StringBuilder clientUrls = new StringBuilder();
    clientContracts.forEach(
        contract ->
            clientUrls.append(
                CLIENT_URL_DEFINITION.replace("/*client*/", contract.getName().toLowerCase())));
    return clientUrls.toString();
  }
}
