package dev.pollito.poof.service.impl;

import dev.pollito.poof.service.GenerateService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GenerateServiceImpl implements GenerateService {
    @Override
    public List<File> generateFiles() throws IOException {
        List<File> files = new ArrayList<>();

        // Example: Creating a few blank text files
        for (int i = 1; i <= 3; i++) {
            File file = File.createTempFile("sample_" + i, ".txt");

            // Using try-with-resources to ensure FileWriter is closed properly
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("file"+i); // Writing blank content
            } catch (IOException e) {
                e.printStackTrace();
            }

            files.add(file);
        }

        return files;
    }
}
