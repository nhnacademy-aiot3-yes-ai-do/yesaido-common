package site.yesaido.common.storage;

public class MinioObjectStorageException extends RuntimeException {
    public MinioObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}