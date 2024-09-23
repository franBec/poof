package dev.pollito.poof.util;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.ProjectMetadata;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class JavaFileUtil {
  private JavaFileUtil() {}

  public static void addFileToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      @NotNull String zipEntryName)
      throws IOException {
    List<Map.Entry<String, Boolean>> conditions =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>(
                "aspect/LoggingAspect.java", generateRequest.getOptions().getLoggingAspect()),
            new AbstractMap.SimpleEntry<>(
                "config/WebConfig.java", generateRequest.getOptions().getAllowCorsFromAnySource()),
            new AbstractMap.SimpleEntry<>(
                "controller/advice/GlobalControllerAdvice.java",
                generateRequest.getOptions().getControllerAdvice()),
            new AbstractMap.SimpleEntry<>(
                "config/LogFilterConfig.java", generateRequest.getOptions().getLogFilter()),
            new AbstractMap.SimpleEntry<>(
                "filter/LogFilter.java", generateRequest.getOptions().getLogFilter()));

    for (Map.Entry<String, Boolean> condition : conditions) {
      if (skipFile(zipEntryName, condition.getKey(), condition.getValue())) {
        return;
      }
    }
    ZipUtil.addFileToZip(
        file,
        zipEntryName,
        zipOutputStream,
        javaReplacements(generateRequest.getProjectMetadata()));
  }

  private static boolean skipFile(
      @NotNull String zipEntryName, String suffix, Boolean generateRequest) {
    return zipEntryName.endsWith(suffix) && !generateRequest;
  }

  private static @NotNull Map<String, String> javaReplacements(
      @NotNull ProjectMetadata projectMetadata) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("/*group*/", projectMetadata.getGroup());
    replacements.put("/*artifact*/", projectMetadata.getArtifact());
    replacements.put("/*Artifact*/", capitalizeFirstLetter(projectMetadata.getArtifact()));

    return replacements;
  }
}
