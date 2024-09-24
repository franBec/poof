package dev.pollito.poof.util;

import dev.pollito.poof.model.Contract;
import dev.pollito.poof.model.GenerateRequest;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class Base64Util {
  private Base64Util() {}

  @SneakyThrows
  public static void addBase64FileToZip(
      @NotNull GenerateRequest generateRequest, ZipOutputStream zipOutputStream) {

    addContractToZip(generateRequest.getContracts().getProviderContract(), zipOutputStream);

    List<Contract> consumerContracts = generateRequest.getContracts().getConsumerContracts();
    if (Objects.nonNull(consumerContracts) && !consumerContracts.isEmpty()) {
      consumerContracts.forEach(
          consumerContract -> addContractToZip(consumerContract, zipOutputStream));
    }
  }

  @SneakyThrows
  private static void addContractToZip(
      @NotNull Contract contract, @NotNull ZipOutputStream zipOutputStream) {
    try (ByteArrayInputStream inputStream =
        new ByteArrayInputStream(Base64.getDecoder().decode(contract.getContent()))) {
      zipOutputStream.putNextEntry(
          new ZipEntry("src/main/resources/openapi/" + contract.getName() + ".yaml"));
      byte[] buffer = new byte[1024];
      int len;
      while ((len = inputStream.read(buffer)) > 0) {
        zipOutputStream.write(buffer, 0, len);
      }
      zipOutputStream.closeEntry();
    }
  }
}
