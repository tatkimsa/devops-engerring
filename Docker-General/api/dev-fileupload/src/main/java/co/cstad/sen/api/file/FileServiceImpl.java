package co.cstad.sen.api.file;

import co.cstad.sen.api.file.web.FileDto;
import co.cstad.sen.utils.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    private final FileUtil fileUtil;

    @Autowired
    public FileServiceImpl(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    @Override
    public FileDto uploadSingle(MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String extension = fileUtil.getExtension(Objects.requireNonNull(file.getOriginalFilename()));
        if (!fileUtil.isExtensionAllowed(extension)) {
            throw new IllegalArgumentException("File extension is not valid");
        }

        if ("zip".equalsIgnoreCase(extension)) {
            return handleZipUpload(file, request);
        } else {
            return handleRegularFileUpload(file, request);
        }
    }

    private FileDto handleZipUpload(MultipartFile file, HttpServletRequest request) {
        FileDto zipFileDto = fileUtil.upload(file, request);
        List<FileDto> extractedFiles = fileUtil.extractZip(file.getOriginalFilename(), "extractedFolder", request);

        return FileDto.builder()
                .name(zipFileDto.getName())
                .extension(zipFileDto.getExtension())
                .size(zipFileDto.getSize())
                .downloadUrl(zipFileDto.getDownloadUrl())
                .additionalInfo("Extracted files: " + extractedFiles.stream()
                        .map(FileDto::getName)
                        .collect(Collectors.joining(", ")))
                .build();
    }

    private FileDto handleRegularFileUpload(MultipartFile file, HttpServletRequest request) {
        return fileUtil.upload(file,request);
    }

    @Override
    public List<FileDto> uploadMultiple(List<MultipartFile> files, HttpServletRequest request) {
        List<FileDto> filesDto = new ArrayList<>();
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Files are empty");
        }
        if (files.size() > 20) {
            throw new IllegalArgumentException("File size is too large");
        }

        for (MultipartFile file : files) {
            String extension = fileUtil.getExtension(Objects.requireNonNull(file.getOriginalFilename()));
            if (!fileUtil.isExtensionAllowed(extension)) {
                throw new IllegalArgumentException("File extension is not valid");
            }
            if (Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                FileDto fileDto = handleRegularFileUpload(file,request);
                filesDto.add(fileDto);
            } else if ("zip".equalsIgnoreCase(extension)) {
                FileDto fileDto = handleZipUpload(file,request);
                filesDto.add(fileDto);
            } else {
                FileDto fileDto = handleRegularFileUpload(file,request);
                filesDto.add(fileDto);
            }
        }
        return filesDto;
    }

    @Override
    public FileDto findByName(String name, HttpServletRequest request) throws IOException {
        Resource resource = fileUtil.load(name);
        if (resource.exists()) {
            return FileDto.builder()
                    .name(resource.getFilename())
                    .extension(fileUtil.getExtension(Objects.requireNonNull(resource.getFilename())))
                    .size(resource.contentLength())
//                    .downloadUrl(fileUtil.getDownloadUrl(resource.getFilename()))
                    .downloadUrl(fileUtil.getDownloadUrl(resource.getFilename(), request))
                    .build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
    }

    @Override
    public void delete(String name, HttpServletRequest request) {
        try {
            FileDto fileDto = findByName(name, request);
            if (fileDto != null) {
                fileUtil.delete(name);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Resource download(String name) throws IOException {
        return fileUtil.load(name);
    }
    @Override
    public Resource loadAsResource(String filename, HttpServletRequest request) throws IOException {
        return fileUtil.load(filename);
    }

    @Override
    public String getViewUrl(String fileName, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return baseUrl + "/api/v1/files/view/" + fileName;
    }
}