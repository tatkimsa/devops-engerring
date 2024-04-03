// FileUtil.java
package co.cstad.sen.utils;

import co.cstad.sen.api.file.web.FileDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class FileUtil {
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "png", "webp", "zip","HEIR");
    @Value("${file.url}")
    private String fileBaseUrl;
    @Value("${file.serverPath}")
    private String fileServerPath;


    public FileDto upload(MultipartFile file, HttpServletRequest request) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String extension = getExtension(originalFileName);

        if (isExtensionAllowed(extension)) {
            String name = generateFileName(originalFileName);
            Long size = file.getSize();
            Path filePath = Paths.get(fileServerPath, name);

            try {
                Files.copy(file.getInputStream(), filePath);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            return FileDto.builder()
                    .name(name)
                    .extension(extension)
                    .size(size)
                    .downloadUrl(getDownloadUrl(name, request))
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File extension is not allowed");
        }
    }

    public List<FileDto> extractZip(String zipFileName, String targetFolderName, HttpServletRequest request) {
        List<FileDto> extractedFiles = new ArrayList<>();

        try (FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(fileServerPath, zipFileName), (ClassLoader) null)) {
            Path zipRoot = fileSystem.getRootDirectories().iterator().next();

            // Walk through the zip file maintaining the directory structure
            Files.walk(zipRoot, Integer.MAX_VALUE)
                    .forEach(source -> {
                        try {
                            Path targetFile = Paths.get(fileServerPath, targetFolderName, zipRoot.relativize(source).toString());

                            if (Files.isDirectory(source)) {
                                Files.createDirectories(targetFile);
                            } else {
                                Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);

                                //                                        .url(getUrl(targetFolderName + "/" + zipRoot.relativize(source).toString()))
                                FileDto fileDto = FileDto.builder()
                                        .name(targetFile.getFileName().toString())
                                        .extension(getExtension(targetFile.getFileName().toString()))
                                        .size(Files.size(targetFile))
//                                        .url(getUrl(targetFolderName + "/" + zipRoot.relativize(source).toString()))
                                        .downloadUrl(getDownloadUrl(targetFolderName + "/" + zipRoot.relativize(source).toString(), request))
                                        .build();

                                extractedFiles.add(fileDto);

                                System.out.println("Copied file to: " + targetFile);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed to extract zip file at path: " + source, e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException("Error extracting zip file: " + e.getMessage(), e);
        }

        return extractedFiles;
    }
    public Resource load(String name) throws IOException {
        Path filePath = Paths.get(fileServerPath, name);
        return new UrlResource(filePath.toUri());
    }

    public void delete(String name) {
        try {
            Path filePath = Paths.get(fileServerPath, name);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public String getExtension(String name) {
        int dotLastIndex = name.lastIndexOf(".");
        return name.substring(dotLastIndex + 1);
    }

    public String getDownloadUrl(String fileName, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return baseUrl + "/api/v1/files/download/" + fileName;
    }


    public boolean isExtensionAllowed(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID() + "_" + originalFileName;
    }
}