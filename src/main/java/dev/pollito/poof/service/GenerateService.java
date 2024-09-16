package dev.pollito.poof.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface GenerateService {
  ByteArrayOutputStream generateFiles() throws IOException;
}
