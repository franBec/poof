package dev.pollito.poof.service.impl;

import static dev.pollito.poof.util.PoofUtil.addBase64FileToZip;
import static dev.pollito.poof.util.PoofUtil.zipFolder;

import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.service.GeneratePoofService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class GeneratePoofServiceImpl implements GeneratePoofService {

  @Override
  @SneakyThrows
  public ByteArrayOutputStream generateFiles(GenerateRequest generateRequest) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    File baseFolder = new ClassPathResource("baseTemplate").getFile();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      zipFolder(baseFolder, "", zipOutputStream, generateRequest);
      addBase64FileToZip(generateRequest, zipOutputStream);
    }

    return byteArrayOutputStream;
  }
}
