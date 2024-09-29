package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.Contract;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class ApplicationYmlAssertions {
  private ApplicationYmlAssertions() {}

  public static void applicationYmlAssertions(
      @NotNull String applicationYmlContent, @NotNull List<Contract> clientContracts) {
    assertTrue(
        applicationYmlContent.contains("name: poof"),
        "application.yml should contain the correct spring application name");
    assertFalse(
        applicationYmlContent.contains("#artifact"),
        "application.yml should not contain the artifact marker");
    assertFalse(
        applicationYmlContent.contains("#clienturls"),
        "application.yml should not contain the clienturls marker");
    clientContracts.forEach(
        contract ->
            assertTrue(
                applicationYmlContent.contains(contract.getName() + ":"),
                "application.yml should contain " + contract.getName() + ":"));

    Matcher matcher =
        Pattern.compile(Pattern.quote("baseUrl: localhost:8080/replace-this-url #replace this url"))
            .matcher(applicationYmlContent);
    int count = 0;
    while (matcher.find()) {
      count++;
    }

    assertEquals(
        clientContracts.size(),
        count,
        "application.yml should contain "
            + clientContracts.size()
            + " baseUrl placeholders, but found "
            + count);
  }
}
