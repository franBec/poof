package dev.pollito.poof.util;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import dev.pollito.poof.consumer.QuadConsumer;
import dev.pollito.poof.model.Contract;
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

  private static final String CONSUMER_EXCEPTION_JAVA = "ConsumerException.java";
  private static final String DEMO_APPLICATION_JAVA = "DemoApplication.java";
  private static final String DEMO_APPLICATION_TESTS_JAVA = "DemoApplicationTests.java";
  private static final String GLOBAL_CONTROLLER_ADVICE_JAVA = "GlobalControllerAdvice.java";
  private static final String CONSUMER_ERROR_DECODER_JAVA = "ConsumerErrorDecoder.java";
  private static final String CONSUMER_CONFIG_PROPERTIES_JAVA = "ConsumerConfigProperties.java";
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
    actionMap.put(CONSUMER_EXCEPTION_JAVA, JavaFileUtil::addConsumerExceptionsToZip);
    actionMap.put(DEMO_APPLICATION_JAVA, JavaFileUtil::addJavaMainToZip);
    actionMap.put(DEMO_APPLICATION_TESTS_JAVA, JavaFileUtil::addJavaMainTestToZip);
    actionMap.put(GLOBAL_CONTROLLER_ADVICE_JAVA, JavaFileUtil::addGlobalControllerAdviceToZip);
    actionMap.put(CONSUMER_ERROR_DECODER_JAVA, JavaFileUtil::addConsumerErrorDecoderToZip);
    actionMap.put(CONSUMER_CONFIG_PROPERTIES_JAVA, JavaFileUtil::addConsumerConfigPropertiesToZip);

    return actionMap;
  }

  @SneakyThrows
  private static void addConsumerConfigPropertiesToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      String zipEntryName) {
    for (Contract contract : request.getContracts().getConsumerContracts()) {
      Map<String, String> replacements = javaReplacements(request.getProjectMetadata());
      replacements.put("/*ConsumerName*/", capitalizeFirstLetter(contract.getName()));
      replacements.put("/*consumerName*/", contract.getName());
      ZipUtil.addFileToZip(
          file,
          zipEntryName.replace(
              CONSUMER_CONFIG_PROPERTIES_JAVA,
              capitalizeFirstLetter(contract.getName()) + "ConfigProperties.java"),
          zipOutputStream,
          replacements);
    }
  }

  @SneakyThrows
  private static void addConsumerErrorDecoderToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      String zipEntryName) {
    for (Contract contract : request.getContracts().getConsumerContracts()) {
      Map<String, String> replacements = javaReplacements(request.getProjectMetadata());
      replacements.put("/*Consumer*/", capitalizeFirstLetter(contract.getName()));
      ZipUtil.addFileToZip(
          file,
          zipEntryName.replace(
              CONSUMER_ERROR_DECODER_JAVA,
              capitalizeFirstLetter(contract.getName()) + "ErrorDecoder.java"),
          zipOutputStream,
          replacements);
    }
  }

  @SneakyThrows
  private static void addGlobalControllerAdviceToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      String zipEntryName) {
    Map<String, String> replacements = javaReplacements(request.getProjectMetadata());
    String consumerExceptionImportsTarget = "/*ConsumerExceptionImports*/";
    String consumerExceptionHandlersTarget = "/*ConsumerExceptionHandlers*/";
    if (request.getContracts().getConsumerContracts().isEmpty()) {
      replacements.put(consumerExceptionImportsTarget, "");
      replacements.put(consumerExceptionHandlersTarget, "");
    } else {
      StringBuilder consumerExceptionsImports = new StringBuilder();
      StringBuilder consumerExceptionsHandlers = new StringBuilder();
      for (Contract contract : request.getContracts().getConsumerContracts()) {
        consumerExceptionsImports
            .append("\nimport ")
            .append(request.getProjectMetadata().getGroup())
            .append(".")
            .append(request.getProjectMetadata().getArtifact())
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
  private static List<Map.Entry<String, Boolean>> buildConditionsMap(@NotNull PoofRequest request) {
    return Arrays.asList(
        new AbstractMap.SimpleEntry<>(
            "aspect/LoggingAspect.java", request.getOptions().getLoggingAspect()),
        new AbstractMap.SimpleEntry<>(
            "config/WebConfig.java", request.getOptions().getAllowCorsFromAnySource()),
        new AbstractMap.SimpleEntry<>(
            "controller/advice/" + GLOBAL_CONTROLLER_ADVICE_JAVA,
            request.getOptions().getControllerAdvice()),
        new AbstractMap.SimpleEntry<>(
            "config/LogFilterConfig.java", request.getOptions().getLogFilter()),
        new AbstractMap.SimpleEntry<>(
            "config/properties/" + CONSUMER_CONFIG_PROPERTIES_JAVA,
            !request.getContracts().getConsumerContracts().isEmpty()),
        new AbstractMap.SimpleEntry<>(
            "errordecoder/" + CONSUMER_ERROR_DECODER_JAVA,
            !request.getContracts().getConsumerContracts().isEmpty()),
        new AbstractMap.SimpleEntry<>(
            "exception/" + CONSUMER_EXCEPTION_JAVA,
            !request.getContracts().getConsumerContracts().isEmpty()),
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
  private static void addConsumerExceptionsToZip(
      ZipOutputStream zipOutputStream,
      @NotNull PoofRequest request,
      File file,
      @NotNull String zipEntryName) {
    for (Contract contract : request.getContracts().getConsumerContracts()) {
      Map<String, String> replacements = javaReplacements(request.getProjectMetadata());
      replacements.put("/*Consumer*/", capitalizeFirstLetter(contract.getName()));
      ZipUtil.addFileToZip(
          file,
          zipEntryName.replace(
              CONSUMER_EXCEPTION_JAVA,
              capitalizeFirstLetter(contract.getName()) + "Exception.java"),
          zipOutputStream,
          replacements);
    }
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
