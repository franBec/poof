package dev.pollito.poof.service.impl;

import static dev.pollito.poof.util.Base64Util.addBase64FilesToZip;
import static dev.pollito.poof.util.PoofUtil.zipFolder;

import dev.pollito.poof.model.PoofRequest;
import dev.pollito.poof.service.GeneratePoofService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class GeneratePoofServiceImpl implements GeneratePoofService {

  @Override
  public ByteArrayOutputStream generateFiles(PoofRequest request) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    File baseFolder = new ClassPathResource("baseTemplate").getFile();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      zipFolder(baseFolder, "", zipOutputStream, request);
      addBase64FilesToZip(request, zipOutputStream);
    }

    return byteArrayOutputStream;
  }
}
