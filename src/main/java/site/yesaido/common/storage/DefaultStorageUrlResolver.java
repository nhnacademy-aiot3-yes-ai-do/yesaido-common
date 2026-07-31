package site.yesaido.common.storage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultStorageUrlResolver implements StorageUrlResolver {
    private final String minioBaseUrl;
    private final String minioBucket;
    private final String localBaseUrl;

    @Override
    public String resolve(StorageType storageType, String objectKey) {
        return switch (storageType) {
            case MINIO -> "%s/%s/%s".formatted(minioBaseUrl, minioBucket, objectKey);
            case LOCAL -> "%s/%s".formatted(localBaseUrl, objectKey);
        };
    }
}
