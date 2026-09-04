package site.yesaido.common.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class MinioObjectStorage {
    private final MinioClient minioClient;
    private final String bucket;

    public void put(String objectKey, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .headers(Map.of("Cache-Control", "public, Max-age=1800, immutable"))
                            .build()
            );
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO 업로드 실패: objectKey=" + objectKey, e);
        }
    }

    public void remove(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO 삭제 실패: objectKey=" + objectKey, e);
        }
    }

    public void removeQuietly(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            remove(objectKey);
        } catch (MinioObjectStorageException e) {
            log.error("MinIO 객체 삭제 실패(고아 객체로 남음): objectKey={}, cause={}", objectKey, e.getMessage());
        }
    }

    public MinioObjectContent get(String objectKey) {
        try (GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectKey).build()
        )) {
            byte[] bytes = response.readAllBytes();
            String contentType = response.headers().get("Content-Type");
            return new MinioObjectContent(bytes, contentType != null ? contentType : "application/octet-stream");
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO 다운로드 실패: objectKey=" + objectKey, e);
        }
    }

    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO 버킷 확인/생성 실패: bucket=" + bucket, e);
        }
    }

    /** 짧은 TTL의 presigned GET URL 발급 — 추후 브라우저/AI 직접 접근용으로 사용 예정 */
    public String presignedGetUrl(String objectKey, Duration ttl) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) ttl.toSeconds(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO presigned URL 발급 실패: objectKey=" + objectKey, e);
        }
    }

    public record MinioObjectContent(byte[] bytes, String contentType) {}
}