package dev.pollito.poof.util;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.ProjectMetadata;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class ApplicationYmlUtil {
  private ApplicationYmlUtil() {}

  public static void addFileToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      String zipEntryName)
      throws IOException {
    ZipUtil.addFileToZip(
        file,
        zipEntryName,
        zipOutputStream,
        applicationYmlReplacements(generateRequest.getProjectMetadata()));
  }

  private static @NotNull Map<String, String> applicationYmlReplacements(
      @NotNull ProjectMetadata projectMetadata) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("#artifact", projectMetadata.getArtifact());

    return replacements;
  }
}
