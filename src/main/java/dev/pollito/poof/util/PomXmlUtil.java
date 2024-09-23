package dev.pollito.poof.util;

import dev.pollito.poof.model.Contract;
import dev.pollito.poof.model.GenerateRequest;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class PomXmlUtil {
  private PomXmlUtil() {}

  public static final String ASPECTJ =
      """

                      <dependency>
                          <groupId>org.aspectj</groupId>
                          <artifactId>aspectjtools</artifactId>
                          <version>1.9.22.1</version>
                      </dependency>
                  """;

  public static final String CONSUMER_DEPENDENCIES =
      """

                              <dependency>
                                  <groupId>javax.annotation</groupId>
                                  <artifactId>javax.annotation-api</artifactId>
                                  <version>1.3.2</version>
                              </dependency>
                              <dependency>
                                  <groupId>io.github.openfeign</groupId>
                                  <artifactId>feign-okhttp</artifactId>
                                  <version>13.4</version>
                              </dependency>
                              <dependency>
                                  <groupId>org.springframework.cloud</groupId>
                                  <artifactId>spring-cloud-starter-openfeign</artifactId>
                                  <version>4.1.3</version>
                              </dependency>
                              <dependency>
                                  <groupId>io.github.openfeign</groupId>
                                  <artifactId>feign-jackson</artifactId>
                                  <version>13.4</version>
                              </dependency>
                              <dependency>
                                  <groupId>com.google.code.findbugs</groupId>
                                  <artifactId>jsr305</artifactId>
                                  <version>3.0.2</version>
                              </dependency>
                              <dependency>
                                  <groupId>org.junit.jupiter</groupId>
                                  <artifactId>junit-jupiter-api</artifactId>
                                  <version>5.11.0</version>
                              </dependency>
                              <dependency>
                                  <groupId>io.github.openfeign</groupId>
                                  <artifactId>feign-gson</artifactId>
                                  <version>13.4</version>
                              </dependency>
                  """;

  public static final String CONSUMER_EXECUTION_BLOCK =
      """

                                      <execution>
                                          <id>consumer generation - <!--name--></id>
                                          <goals>
                                              <goal>generate</goal>
                                          </goals>
                                          <configuration>
                                              <inputSpec>${project.basedir}/src/main/resources/openapi/<!--name-->.yaml</inputSpec>
                                              <generatorName>java</generatorName>
                                              <library>feign</library>
                                              <output>${project.build.directory}/generated-sources/openapi/</output>
                                              <apiPackage><!--apiPackage--></apiPackage>
                                              <modelPackage><!--modelPackage--></modelPackage>
                                              <configOptions>
                                                  <feignClient>true</feignClient>
                                                  <interfaceOnly>true</interfaceOnly>
                                                  <useEnumCaseInsensitive>true</useEnumCaseInsensitive>
                                              </configOptions>
                                          </configuration>
                                      </execution>
                  """;

  static @NotNull Map<String, String> pomXmlReplacements(@NotNull GenerateRequest generateRequest) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("<!--groupId-->", generateRequest.getProjectMetadata().getGroup());
    replacements.put("<!--artifactId-->", generateRequest.getProjectMetadata().getArtifact());
    replacements.put("<!--description-->", generateRequest.getProjectMetadata().getDescription());
    replacements.put("<!--aspectj-->", aspectjReplacement(generateRequest));
    replacements.put(
        "<!--consumer dependencies-->", consumerDependenciesReplacement(generateRequest));
    replacements.put("<!--consumer generation-->", consumerGenerationReplacement(generateRequest));

    return replacements;
  }

  private static String consumerDependenciesReplacement(@NotNull GenerateRequest generateRequest) {
    return generateRequest.getContracts().getConsumerContracts().isEmpty()
        ? ""
        : CONSUMER_DEPENDENCIES;
  }

  private static @NotNull String aspectjReplacement(@NotNull GenerateRequest generateRequest) {
    return Boolean.TRUE.equals(generateRequest.getOptions().getLoggingAspect()) ? ASPECTJ : "";
  }

  private static @NotNull String consumerGenerationReplacement(
      @NotNull GenerateRequest generateRequest) {
    if (generateRequest.getContracts().getConsumerContracts().isEmpty()) {
      return "";
    }

    String uri = "com.example";
    StringBuilder s = new StringBuilder();
    for (Contract contract : generateRequest.getContracts().getConsumerContracts()) {
      s.append(
          CONSUMER_EXECUTION_BLOCK
              .replace("<!--name-->", contract.getName())
              .replace("<!--apiPackage-->", uri + ".api")
              .replace("<!--modelPackage-->", uri + ".models"));
    }

    return s.toString();
  }
}
