package dev.pollito.poof.util;

import dev.pollito.poof.model.PoofRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class ApplicationYmlUtil {
  private ApplicationYmlUtil() {}

  public static void addFileToZip(
      ZipOutputStream zipOutputStream, @NotNull PoofRequest request, File file, String zipEntryName)
      throws IOException {
    ZipUtil.addFileToZip(file, zipEntryName, zipOutputStream, applicationYmlReplacements(request));
  }

  private static @NotNull Map<String, String> applicationYmlReplacements(
      @NotNull PoofRequest request) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("#artifact", request.getProjectMetadata().getArtifact());
    return replacements;
  }
}
