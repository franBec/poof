package dev.pollito.poof.service;

import dev.pollito.poof.model.PoofRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface GeneratePoofService {
  ByteArrayOutputStream generateFiles(PoofRequest request) throws IOException;
}
