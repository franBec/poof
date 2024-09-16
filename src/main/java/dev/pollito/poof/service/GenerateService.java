package dev.pollito.poof.service;

import dev.pollito.poof.model.GenerateRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface GenerateService {
  ByteArrayOutputStream generateFiles(GenerateRequest generateRequest) throws IOException;
}
