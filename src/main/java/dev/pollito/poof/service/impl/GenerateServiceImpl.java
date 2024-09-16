package dev.pollito.poof.service.impl;

import dev.pollito.poof.service.GenerateService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class GenerateServiceImpl implements GenerateService {
  @Override
  public List<File> generateFiles() throws IOException {
    List<File> files = new ArrayList<>();

    // Load the HELP.md file from resources/baseFiles
    ClassPathResource helpFileResource = new ClassPathResource("baseFiles/HELP.md");

    // Check if HELP.md exists
    if (!helpFileResource.exists()) {
      throw new FileNotFoundException("HELP.md not found in resources/baseFiles!");
    }

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
        files.add(file); // Add the blank text file to the list
      } catch (IOException e) {
        e.printStackTrace();
        // Handle the exception as per your requirement
      }

      // Copy HELP.md into the created folder
      File helpFile = new File(folder, "HELP.md");
      try (InputStream helpFileInputStream = helpFileResource.getInputStream()) {
        Files.copy(helpFileInputStream, helpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Copied HELP.md to " + helpFile.getAbsolutePath()); // Debugging log
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error copying HELP.md to " + helpFile.getAbsolutePath());
        // Handle the exception as per your requirement
      }
    }

    return files;
  }
}
