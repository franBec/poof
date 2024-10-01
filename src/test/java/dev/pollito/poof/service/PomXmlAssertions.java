package dev.pollito.poof.service;

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
    boolean expected = request.getOptions().getConsumeOtherServices();
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
    boolean expected = request.getOptions().getConsumeOtherServices();

    dependencies.forEach(
        dependency ->
            assertEquals(
                expected,
                pomXmlContent.contains(dependency),
                "pom.xml should " + (expected ? "" : "not ") + "contain " + dependency));
  }

  private static void pomXmlProviderGenerationAssertions(@NotNull String pomXmlContent) {
    assertTrue(
        pomXmlContent.contains("<id>provider generation - poof.yaml</id>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution id");
    assertTrue(
        pomXmlContent.contains(
            "<inputSpec>${project.basedir}/src/main/resources/openapi/poof.yaml</inputSpec>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration inputSpec");
    assertTrue(
        pomXmlContent.contains("<apiPackage>${project.groupId}.poof.api</apiPackage>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration apiPackage");
    assertTrue(
        pomXmlContent.contains("<modelPackage>${project.groupId}.poof.model</modelPackage>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration modelPackage");
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
        pomXmlContent.contains("<groupId>dev.pollito</groupId>"),
        "pom.xml should contain the correct <groupId>");
    assertTrue(
        pomXmlContent.contains("<artifactId>poof</artifactId>"),
        "pom.xml should contain the correct <artifactId>");
    assertTrue(
        pomXmlContent.contains("<name>poof</name>"), "pom.xml should contain the correct <name>");
    assertTrue(
        pomXmlContent.contains(
            "<description>poof - Pollito Over Opinionated Framework</description>"),
        "pom.xml should contain the correct <description>");
  }
}
