package site.yesaido.common.storage;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class ObjectKeyGenerator {
    public String generate(String domain, Long ownerId, String originalFilename) {
        String ext = extractExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return ext.isEmpty()
                ? "%s/%d/%s".formatted(domain, ownerId, uuid)
                : "%s/%d/%s.%s".formatted(domain, ownerId, uuid, ext);
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot == -1 || dot == filename.length() - 1) ? "" : filename.substring(dot + 1).toLowerCase();
    }
}
