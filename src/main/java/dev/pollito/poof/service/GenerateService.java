package dev.pollito.poof.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface GenerateService {
    List<File> generateFiles() throws IOException;
}
