package co.cstad.sen.api.file;

import co.cstad.sen.api.file.web.FileDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    FileDto uploadSingle(MultipartFile file, HttpServletRequest request);
    List<FileDto> uploadMultiple(List<MultipartFile> files, HttpServletRequest request);
    FileDto findByName(String name, HttpServletRequest request) throws IOException;
    void delete(String name, HttpServletRequest request);
    Resource download(String name) throws IOException;
    Resource loadAsResource(String filename, HttpServletRequest request) throws IOException; // New method
    String getViewUrl(String fileName, HttpServletRequest request);


}
