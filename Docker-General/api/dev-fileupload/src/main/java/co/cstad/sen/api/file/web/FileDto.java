
package co.cstad.sen.api.file.web;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDto {
    private final String extension;
    private final String name;
    private final Long size;
    private final String downloadUrl;
    private final String additionalInfo;
    private String viewUrl;

}