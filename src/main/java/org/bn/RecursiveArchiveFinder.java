package org.bn;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RecursiveArchiveFinder {

    private static List<String[]> results;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar ArchiveFinder.jar <search_path> <search_string> <archive_extensions>");
            return;
        }

        results = new ArrayList<>();
        String searchPath = args[0];
        String searchString = args[1];
        String[] archiveExtensions = args[2].split(",");

        Path startPath = Paths.get(searchPath);

        searchFilesRecursively(startPath, searchString, archiveExtensions);

        saveResultsToCSV("search_results.csv");
    }
    private static void searchFilesRecursively(Path path, String searchString, String[] archiveExtensions) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (WildcardUtils.isMatch(searchString, file.getFileName().toString())) {
                        addResult(file);
                    }

                    String extension = getFileExtension(file);
                    for (String ext : archiveExtensions) {
                        if (ext.equalsIgnoreCase(extension)) {
                            searchFilesInArchive(file, searchString);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error walking through files: " + e.getMessage());
        }
    }

    private static void searchFilesInArchive(Path archivePath, String searchString) {
        try (InputStream fi = Files.newInputStream(archivePath);
             BufferedInputStream bi = new BufferedInputStream(fi)) {

            String extension = getFileExtension(archivePath).toLowerCase();
            switch (extension) {
                case "zip":
                case "jar":
                case "war":
                case "ear":
                case "rar":
                    try (ArchiveInputStream<?> i = createArchiveInputStream(archivePath, bi)) {
                        ArchiveEntry entry;
                        while ((entry = i.getNextEntry()) != null) {
                            if (WildcardUtils.isMatch(searchString, entry.getName())) {
                                addResult(archivePath, entry);
                            }
                        }
                    }
                    break;
                default:
//                    System.err.println("File extension not found: " + archivePath);
                    break;
            }
        } catch (IOException e) {
            System.err.println("Error processing archive: " + archivePath + " - " + e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private static ArchiveInputStream createArchiveInputStream(Path archivePath, BufferedInputStream bi) throws IOException {
        String extension = getFileExtension(archivePath).toLowerCase();
        switch (extension) {
            case "zip":
            case "rar":
                return new ZipArchiveInputStream(bi);  // RAR uses ZipArchiveInputStream for simplicity
            case "tar":
                return new TarArchiveInputStream(bi);
            case "jar":
            case "ear":
            case "war":
                return new JarArchiveInputStream(bi);  // EAR and WAR are treated like JAR files
            default:
                return null; // Unsupported archive type
        }
    }

    private static String getFileExtension(Path path) {
        String name = path.getFileName().toString();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1);
    }

    private static void addResult(Path file) {
        try {
            File f = file.toFile();
            String[] result = {
                    f.getAbsolutePath(),
                    Long.toString(f.length()),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(f.lastModified())
            };
            results.add(result);
        } catch (Exception e) {
            System.err.println("Error adding file result: " + file + " - " + e.getMessage());
        }
    }

    private static void addResult(Path archivePath, ArchiveEntry entry) {
        try {
            String[] result = {
                    archivePath.toString() + "!" + entry.getName(),
                    Long.toString(entry.getSize()),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.getLastModifiedDate())
            };
            results.add(result);
        } catch (Exception e) {
            System.err.println("Error adding archive entry result: " + archivePath + "!" + entry.getName() + " - " + e.getMessage());
        }
    }

    private static void saveResultsToCSV(String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            StringBuilder sb = new StringBuilder();
            sb.append("File Path");
            sb.append(',');
            sb.append("Size (bytes)");
            sb.append(',');
            sb.append("Last Modified");
            sb.append('\n');

            for (String[] result : results) {
                sb.append(String.join(",", result));
                sb.append('\n');
            }

            writer.write(sb.toString());
            System.out.println("Results saved to " + fileName);
        } catch (FileNotFoundException e) {
            System.err.println("Error saving results to CSV: " + e.getMessage());
        }
    }
}
