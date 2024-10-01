package dev.pollito.poof.util;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import dev.pollito.poof.consumer.QuadConsumer;
import dev.pollito.poof.model.PoofRequest;
import dev.pollito.poof.model.ProjectMetadata;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class JavaFileUtil {

  private static final String DEMO_APPLICATION_JAVA = "DemoApplication.java";
  private static final String DEMO_APPLICATION_TESTS_JAVA = "DemoApplicationTests.java";
  private static final String DEMO_CONTROLLER = "DemoController.java";

  private JavaFileUtil() {}

  public static void addFileToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      @NotNull String zipEntryName)
      throws IOException {
    for (Map.Entry<String, Boolean> condition : buildConditionsMap(request)) {
      if (skipFile(zipEntryName, condition.getKey(), condition.getValue())) {
        return;
      }
    }

    Map<String, QuadConsumer<ZipOutputStream, PoofRequest, File, String>> actionMap =
        buildActionMap();
    for (Map.Entry<String, QuadConsumer<ZipOutputStream, PoofRequest, File, String>> entry :
        actionMap.entrySet()) {
      if (zipEntryName.contains(entry.getKey())) {
        entry.getValue().accept(zipOutputStream, request, file, zipEntryName);
        return;
      }
    }

    ZipUtil.addFileToZip(
        file, zipEntryName, zipOutputStream, javaReplacements(request.getProjectMetadata()));
  }

  private static @NotNull Map<String, QuadConsumer<ZipOutputStream, PoofRequest, File, String>>
      buildActionMap() {
    Map<String, QuadConsumer<ZipOutputStream, PoofRequest, File, String>> actionMap =
        new HashMap<>();
    actionMap.put(DEMO_APPLICATION_JAVA, JavaFileUtil::addJavaMainToZip);
    actionMap.put(DEMO_APPLICATION_TESTS_JAVA, JavaFileUtil::addJavaMainTestToZip);
    actionMap.put(DEMO_CONTROLLER, JavaFileUtil::addControllerToZip);

    return actionMap;
  }

  @NotNull
  private static List<Map.Entry<String, Boolean>> buildConditionsMap(@NotNull PoofRequest request) {
    return Arrays.asList(
        new AbstractMap.SimpleEntry<>(
            "aspect/LoggingAspect.java", request.getOptions().getLoggingAspect()),
        new AbstractMap.SimpleEntry<>(
            "config/WebConfig.java", request.getOptions().getAllowCorsFromAnySource()),
        new AbstractMap.SimpleEntry<>(
            "controller/advice/GlobalControllerAdvice.java",
            request.getOptions().getControllerAdvice()),
        new AbstractMap.SimpleEntry<>(
            "config/LogFilterConfig.java", request.getOptions().getLogFilter()),
        new AbstractMap.SimpleEntry<>(
            "filter/LogFilter.java", request.getOptions().getLogFilter()));
  }

  @SneakyThrows
  private static void addJavaMainTestToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      @NotNull String zipEntryName) {
    ZipUtil.addFileToZip(
        file,
        zipEntryName.replace(
            DEMO_APPLICATION_TESTS_JAVA,
            capitalizeFirstLetter(request.getProjectMetadata().getArtifact())
                + "ApplicationTests.java"),
        zipOutputStream,
        javaReplacements(request.getProjectMetadata()));
  }

  @SneakyThrows
  private static void addJavaMainToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      @NotNull String zipEntryName) {
    ZipUtil.addFileToZip(
        file,
        zipEntryName.replace(
            DEMO_APPLICATION_JAVA,
            capitalizeFirstLetter(request.getProjectMetadata().getArtifact()) + "Application.java"),
        zipOutputStream,
        javaReplacements(request.getProjectMetadata()));
  }

  @SneakyThrows
  private static void addControllerToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      @NotNull String zipEntryName) {
    ZipUtil.addFileToZip(
        file,
        zipEntryName.replace(
            DEMO_CONTROLLER,
            capitalizeFirstLetter(request.getProjectMetadata().getArtifact()) + "Controller.java"),
        zipOutputStream,
        javaReplacements(request.getProjectMetadata()));
  }

  private static boolean skipFile(@NotNull String zipEntryName, String key, Boolean condition) {
    return zipEntryName.endsWith(key) && !condition;
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
