package dev.pollito.poof.util;

import dev.pollito.poof.model.PoofRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class PomXmlUtil {
  private PomXmlUtil() {}

  public static final String ASPECTJ_DEPENDENCY =
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

                                      <!--<execution>
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
                                      </execution>-->
                  """;

  public static void addFileToZip(
      ZipOutputStream zipOutputStream, PoofRequest request, File file, String zipEntryName)
      throws IOException {
    ZipUtil.addFileToZip(file, zipEntryName, zipOutputStream, pomXmlReplacements(request));
  }

  private static @NotNull Map<String, String> pomXmlReplacements(@NotNull PoofRequest request) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("<!--groupId-->", request.getProjectMetadata().getGroup());
    replacements.put("<!--artifactId-->", request.getProjectMetadata().getArtifact());
    replacements.put("<!--description-->", request.getProjectMetadata().getDescription());
    replacements.put("<!--aspectj-->", aspectjReplacement(request));
    replacements.put("<!--consumer dependencies-->", consumerDependenciesReplacement(request));
    replacements.put("<!--consumer generation-->", consumerGenerationReplacement(request));

    return replacements;
  }

  private static String consumerDependenciesReplacement(@NotNull PoofRequest request) {
    return Boolean.TRUE.equals(request.getOptions().getConsumesOtherServicesWithOAS())
        ? CONSUMER_DEPENDENCIES
        : "";
  }

  private static @NotNull String aspectjReplacement(@NotNull PoofRequest request) {
    return Boolean.TRUE.equals(request.getOptions().getLoggingAspect()) ? ASPECTJ_DEPENDENCY : "";
  }

  private static @NotNull String consumerGenerationReplacement(@NotNull PoofRequest request) {
    return Boolean.TRUE.equals(request.getOptions().getConsumesOtherServicesWithOAS())
        ? CONSUMER_EXECUTION_BLOCK
        : "";
  }
}
