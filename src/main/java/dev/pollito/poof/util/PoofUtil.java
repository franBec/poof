package dev.pollito.poof.util;

import dev.pollito.poof.model.PoofRequest;
import dev.pollito.poof.model.ProjectMetadata;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class PoofUtil {
  private PoofUtil() {}

  public static final String SRC_MAIN_JAVA_COM_EXAMPLE_DEMO = "src/main/java/com/example/demo";

  public static void zipFolder(
      @NotNull File folder,
      String parentFolder,
      ZipOutputStream zipOutputStream,
      PoofRequest request)
      throws IOException {
    File[] files = folder.listFiles();

    if (Objects.nonNull(files)) {
      for (File file : files) {
        String zipEntryName = getNewZipEntryName(parentFolder, file, request.getProjectMetadata());

        if (file.isDirectory()) {
          zipFolder(file, zipEntryName + "/", zipOutputStream, request);
        } else if ("pom.xml".equals(file.getName())) {
          PomXmlUtil.addFileToZip(zipOutputStream, request, file, zipEntryName);
        } else if ("application.yml".equals(file.getName())) {
          ApplicationYmlUtil.addFileToZip(zipOutputStream, request, file, zipEntryName);
        } else if (file.getName().endsWith(".java")) {
          JavaFileUtil.addFileToZip(zipOutputStream, request, file, zipEntryName);
        } else {
          ZipUtil.addFileToZip(file, zipEntryName, zipOutputStream);
        }
      }
    }
  }

  private static @NotNull String getNewZipEntryName(
      @NotNull String parentFolder, File file, @NotNull ProjectMetadata projectMetadata) {
    String groupPath = projectMetadata.getGroup().replace('.', '/');
    String artifact = projectMetadata.getArtifact();
    String zipEntryName;

    if (parentFolder.startsWith(SRC_MAIN_JAVA_COM_EXAMPLE_DEMO)) {
      zipEntryName =
          parentFolder.replace(
                  SRC_MAIN_JAVA_COM_EXAMPLE_DEMO, "src/main/java/" + groupPath + "/" + artifact)
              + file.getName();
    } else if (parentFolder.startsWith("src/test/java/com/example/demo")) {
      zipEntryName =
          parentFolder.replace(
                  "src/test/java/com/example/demo", "src/test/java/" + groupPath + "/" + artifact)
              + file.getName();
    } else {
      zipEntryName = parentFolder + file.getName();
    }

    return zipEntryName;
  }
}
