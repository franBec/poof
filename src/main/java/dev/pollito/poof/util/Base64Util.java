package dev.pollito.poof.util;

import dev.pollito.poof.model.Contract;
import dev.pollito.poof.model.GenerateRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class Base64Util {
  private Base64Util() {}

  public static void addBase64FilesToZip(
      @NotNull GenerateRequest generateRequest, ZipOutputStream zipOutputStream)
      throws IOException {

    addContractToZip(generateRequest.getContracts().getProviderContract(), zipOutputStream);

    List<Contract> consumerContracts = generateRequest.getContracts().getConsumerContracts();
    if (Objects.nonNull(consumerContracts) && !consumerContracts.isEmpty()) {
      for (Contract consumerContract : consumerContracts) {
        addContractToZip(consumerContract, zipOutputStream);
      }
    }
  }

  private static void addContractToZip(
      @NotNull Contract contract, @NotNull ZipOutputStream zipOutputStream) throws IOException {
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
