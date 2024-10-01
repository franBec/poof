package dev.pollito.poof.service;

import static dev.pollito.poof.service.GeneratePoofServiceTest.PROJECT_METADATA_ARTIFACT;
import static dev.pollito.poof.service.GeneratePoofServiceTest.PROJECT_METADATA_DESCRIPTION;
import static dev.pollito.poof.service.GeneratePoofServiceTest.PROJECT_METADATA_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.PoofRequest;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PomXmlAssertions {
  private PomXmlAssertions() {}

  public static void pomXmlAssertions(@NotNull PoofRequest request, String pomXmlContent) {
    pomXmlBasicInfoAssertions(pomXmlContent);
    pomXmlAspectjAssertions(request, pomXmlContent);
    pomXmlProviderGenerationAssertions(pomXmlContent);
    pomXmlConsumerGenerationDependenciesAssertions(request, pomXmlContent);
    pomXmlConsumerGenerationPluginConfigAssertions(request, pomXmlContent);
  }

  private static void pomXmlConsumerGenerationPluginConfigAssertions(
      @NotNull PoofRequest request, @NotNull String pomXmlContent) {
    boolean expected = request.getOptions().getConsumesOtherServicesWithOAS();
    String idTag = "<id>consumer generation - <!--name--></id>";
    assertEquals(
        expected,
        pomXmlContent.contains(idTag),
        "pom.xml should " + (expected ? "" : "not ") + "contain " + idTag);
  }

  private static void pomXmlConsumerGenerationDependenciesAssertions(
      @NotNull PoofRequest request, @NotNull String pomXmlContent) {
    List<String> dependencies =
        List.of(
            "<artifactId>javax.annotation-api</artifactId>",
            "<artifactId>feign-okhttp</artifactId>",
            "<artifactId>spring-cloud-starter-openfeign</artifactId>",
            "<artifactId>feign-jackson</artifactId>",
            "<artifactId>jsr305</artifactId>",
            "<artifactId>junit-jupiter-api</artifactId>",
            "<artifactId>feign-gson</artifactId>");
    boolean expected = request.getOptions().getConsumesOtherServicesWithOAS();

    dependencies.forEach(
        dependency ->
            assertEquals(
                expected,
                pomXmlContent.contains(dependency),
                "pom.xml should " + (expected ? "" : "not ") + "contain " + dependency));
  }

  private static void pomXmlProviderGenerationAssertions(@NotNull String pomXmlContent) {
    assertTrue(
        pomXmlContent.contains("<id>provider generation - <!--name of the OAS file-->.yaml</id>"),
        "pom.xml org.openapitools:openapi-generator-maven-plugin provider execution should contain the id tag");
    assertTrue(
        pomXmlContent.contains(
            "<inputSpec>${project.basedir}/src/main/resources/openapi/<!--name of the OAS file-->.yaml</inputSpec>"),
        "pom.xml org.openapitools:openapi-generator-maven-plugin provider execution should contain the inputSpec tag");
  }

  private static void pomXmlAspectjAssertions(
      @NotNull PoofRequest request, @NotNull String pomXmlContent) {
    String aspectjArtifactId = "<artifactId>aspectjtools</artifactId>";
    boolean expected = request.getOptions().getLoggingAspect();

    assertEquals(
        expected,
        pomXmlContent.contains(aspectjArtifactId),
        "pom.xml should " + (expected ? "" : "not ") + "contain artifactId org.aspectj");
  }

  private static void pomXmlBasicInfoAssertions(@NotNull String pomXmlContent) {
    assertTrue(
        pomXmlContent.contains("<groupId>" + PROJECT_METADATA_GROUP + "</groupId>"),
        "pom.xml should contain the correct <groupId>");
    assertTrue(
        pomXmlContent.contains("<artifactId>" + PROJECT_METADATA_ARTIFACT + "</artifactId>"),
        "pom.xml should contain the correct <artifactId>");
    assertTrue(
        pomXmlContent.contains("<name>" + PROJECT_METADATA_ARTIFACT + "</name>"),
        "pom.xml should contain the correct <name>");
    assertTrue(
        pomXmlContent.contains("<description>" + PROJECT_METADATA_DESCRIPTION + "</description>"),
        "pom.xml should contain the correct <description>");
  }
}
