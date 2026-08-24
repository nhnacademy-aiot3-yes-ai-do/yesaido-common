package site.yesaido.common.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class MinioObjectStorage {
    private final MinioClient minioClient;
    private final String bucket;

    public void put(String objectKey, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO 업로드 실패: objectKey=" + objectKey, e);
        }
    }

    public void remove(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO 삭제 실패: objectKey=" + objectKey, e);
        }
    }

    public void removeQuietly(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            remove(objectKey);
        } catch (Exception e) {
            log.error("MinIO 객체 삭제 실패(고아 객체로 남음): objectKey=" + objectKey, e);
        }
    }

    public byte[] getBytes(String objectKey) {
        try (GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectKey).build()
        )) {
            return response.readAllBytes();
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

    public String presignedGetUrl(String objectKey, Duration ttl) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) ttl.toSeconds(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioObjectStorageException("MinIO presigned URL 발급 실패: objectKey=" + objectKey, e);
        }
    }
}
