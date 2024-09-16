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

        // Example: Creating a few blank text files inside folders
        for (int i = 1; i <= 3; i++) {
            // Define the folder name (without extension)
            String folderName = "sample_" + i;

            // Create a temporary directory with the folder name
            File folder = new File(System.getProperty("java.io.tmpdir"), folderName);
            if (!folder.exists() && !folder.mkdir()) {
                throw new IOException("Failed to create directory: " + folder.getAbsolutePath());
            }

            // Define the file inside the created folder
            File file = new File(folder, folderName + ".txt");

            // Using try-with-resources to ensure FileWriter is closed properly
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(""); // Writing blank content
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as per your requirement
            }

            files.add(file);
        }

        return files;
    }
}
