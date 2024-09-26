package dev.pollito.poof.util;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import dev.pollito.poof.model.Contract;
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

  private static final String CONSUMER_EXCEPTION_JAVA = "ConsumerException.java";
  private static final String DEMO_APPLICATION_JAVA = "DemoApplication.java";
  private static final String DEMO_APPLICATION_TESTS_JAVA = "DemoApplicationTests.java";
  private static final String GLOBAL_CONTROLLER_ADVICE_JAVA = "GlobalControllerAdvice.java";
  private static final String GLOBAL_CONTROLLER_ADVICE_EXCEPTION_BLOCK =
      """

            @ExceptionHandler(/*Exception*/.class)
              public ProblemDetail handle(@NotNull /*Exception*/ e) {
                log.error("/*Exception*/ being handled", e);
                return buildProblemDetail(e);
              }
          """;

  private JavaFileUtil() {}

  public static void addFileToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      @NotNull String zipEntryName)
      throws IOException {
    for (Map.Entry<String, Boolean> condition : buildConditionsMap(generateRequest)) {
      if (skipFile(zipEntryName, condition.getKey(), condition.getValue())) {
        return;
      }
    }
    if (zipEntryName.contains(CONSUMER_EXCEPTION_JAVA)) {
      addConsumerExceptionsToZip(zipOutputStream, generateRequest, file, zipEntryName);
    } else if (zipEntryName.contains(DEMO_APPLICATION_JAVA)) {
      addJavaMainToZip(zipOutputStream, generateRequest, file, zipEntryName);
    } else if (zipEntryName.contains(DEMO_APPLICATION_TESTS_JAVA)) {
      addJavaMainTestToZip(zipOutputStream, generateRequest, file, zipEntryName);
    } else if (zipEntryName.contains(GLOBAL_CONTROLLER_ADVICE_JAVA)) {
      addGlobalControllerAdviceToZip(zipOutputStream, generateRequest, file, zipEntryName);
    } else {
      ZipUtil.addFileToZip(
          file,
          zipEntryName,
          zipOutputStream,
          javaReplacements(generateRequest.getProjectMetadata()));
    }
  }

  private static void addGlobalControllerAdviceToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      String zipEntryName)
      throws IOException {
    Map<String, String> replacements = javaReplacements(generateRequest.getProjectMetadata());
    String consumerExceptionImportsTarget = "/*ConsumerExceptionImports*/";
    String consumerExceptionHandlersTarget = "/*ConsumerExceptionHandlers*/";
    if (generateRequest.getContracts().getConsumerContracts().isEmpty()) {
      replacements.put(consumerExceptionImportsTarget, "");
      replacements.put(consumerExceptionHandlersTarget, "");
    } else {
      StringBuilder consumerExceptionsImports = new StringBuilder();
      StringBuilder consumerExceptionsHandlers = new StringBuilder();
      for (Contract contract : generateRequest.getContracts().getConsumerContracts()) {
        consumerExceptionsImports
            .append("\nimport ")
            .append(generateRequest.getProjectMetadata().getGroup())
            .append(".")
            .append(generateRequest.getProjectMetadata().getArtifact())
            .append(".exception.")
            .append(capitalizeFirstLetter(contract.getName()))
            .append("Exception;");

        consumerExceptionsHandlers.append(
            GLOBAL_CONTROLLER_ADVICE_EXCEPTION_BLOCK.replace(
                "/*Exception*/", capitalizeFirstLetter(contract.getName()) + "Exception"));
      }
      replacements.put(consumerExceptionImportsTarget, consumerExceptionsImports.toString());
      replacements.put(consumerExceptionHandlersTarget, consumerExceptionsHandlers.toString());
    }
    ZipUtil.addFileToZip(file, zipEntryName, zipOutputStream, replacements);
  }

  @NotNull
  private static List<Map.Entry<String, Boolean>> buildConditionsMap(
      @NotNull GenerateRequest generateRequest) {
    return Arrays.asList(
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
            "exception/ConsumerException.java",
            !generateRequest.getContracts().getConsumerContracts().isEmpty()),
        new AbstractMap.SimpleEntry<>(
            "filter/LogFilter.java", generateRequest.getOptions().getLogFilter()));
  }

  private static void addJavaMainTestToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      @NotNull String zipEntryName)
      throws IOException {
    ZipUtil.addFileToZip(
        file,
        zipEntryName.replace(
            DEMO_APPLICATION_TESTS_JAVA,
            capitalizeFirstLetter(generateRequest.getProjectMetadata().getArtifact())
                + "ApplicationTests.java"),
        zipOutputStream,
        javaReplacements(generateRequest.getProjectMetadata()));
  }

  private static void addJavaMainToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      @NotNull String zipEntryName)
      throws IOException {
    ZipUtil.addFileToZip(
        file,
        zipEntryName.replace(
            DEMO_APPLICATION_JAVA,
            capitalizeFirstLetter(generateRequest.getProjectMetadata().getArtifact())
                + "Application.java"),
        zipOutputStream,
        javaReplacements(generateRequest.getProjectMetadata()));
  }

  private static void addConsumerExceptionsToZip(
      ZipOutputStream zipOutputStream,
      @NotNull GenerateRequest generateRequest,
      File file,
      @NotNull String zipEntryName)
      throws IOException {
    for (Contract contract : generateRequest.getContracts().getConsumerContracts()) {
      Map<String, String> replacements = javaReplacements(generateRequest.getProjectMetadata());
      replacements.put("Consumer", contract.getName());
      ZipUtil.addFileToZip(
          file,
          zipEntryName.replace(
              CONSUMER_EXCEPTION_JAVA,
              capitalizeFirstLetter(contract.getName()) + "Exception.java"),
          zipOutputStream,
          replacements);
    }
  }

  private static boolean skipFile(
      @NotNull String zipEntryName, String key, Boolean generateRequest) {
    return zipEntryName.endsWith(key) && !generateRequest;
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
