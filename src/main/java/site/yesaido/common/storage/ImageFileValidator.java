package site.yesaido.common.storage;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@UtilityClass
public class ImageFileValidator {
    public boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    public boolean isAllowedContentType(MultipartFile file, Set<String> allowedContentTypes) {
        String contentType = file.getContentType();
        return contentType != null && allowedContentTypes.contains(contentType);
    }

    public boolean exceedsMaxSize(MultipartFile file, long maxFileSizeBytes) {
        return file.getSize() > maxFileSizeBytes;
    }
}