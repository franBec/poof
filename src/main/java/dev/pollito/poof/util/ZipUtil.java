package dev.pollito.poof.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class ZipUtil {
  private ZipUtil() {}

  public static void addFileToZip(
      File file, String zipEntryName, @NotNull ZipOutputStream zipOutputStream) throws IOException {
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      ZipEntry zipEntry = new ZipEntry(zipEntryName);
      zipOutputStream.putNextEntry(zipEntry);

      writeToFileStream(fileInputStream, zipOutputStream);
      zipOutputStream.closeEntry();
    }
  }

  public static void addFileToZip(
      @NotNull File file,
      String zipEntryName,
      @NotNull ZipOutputStream zipOutputStream,
      @NotNull Map<String, String> replacements)
      throws IOException {
    String content = Files.readString(file.toPath());

    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      content = content.replace(entry.getKey(), entry.getValue());
    }

    zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
    zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    zipOutputStream.closeEntry();
  }

  private static void writeToFileStream(
      @NotNull FileInputStream fileInputStream, ZipOutputStream zipOutputStream)
      throws IOException {
    byte[] buffer = new byte[1024];
    int length;
    while ((length = fileInputStream.read(buffer)) > 0) {
      zipOutputStream.write(buffer, 0, length);
    }
  }
}
