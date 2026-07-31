package site.yesaido.common.storage;

public interface StorageUrlResolver {
    String resolve(StorageType storageType, String objectKey);
}
