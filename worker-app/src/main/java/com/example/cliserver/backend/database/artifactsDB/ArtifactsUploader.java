package com.example.cliserver.backend.database.artifactsDB;

import com.example.cliserver.backend.utils.Constants;
import com.example.cliserver.backend.utils.YamlConfigLoader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;


/**
 * A utility class to handle uploading artifact files to a MinIO bucket.
 * <p>
 * This class supports uploading:
 * - Single files
 * - Entire directories (recursively)
 * - Wildcard-based file patterns (e.g., *.json)
 */
public class ArtifactsUploader {
    private MinioClient minioClient;

    /**
     * Constructs a new {@code ArtifactsUploader} instance.
     * Initializes the MinIO client using configuration loaded from the YAML file.
     */
    public ArtifactsUploader() {
        try {
            initializeMinioClient();
        } catch (IOException e) {
            System.err.println("Failed to initialize MinIO client: " + e.getMessage());
        }
    }

    /**
     * Initializes the MinIO client using values from the configuration file.
     *
     * @throws IOException if the configuration cannot be read
     */
    private void initializeMinioClient() throws IOException {
        this.minioClient = MinioClient.builder()
                .endpoint(Objects.requireNonNull(
                        YamlConfigLoader.getConfigValue("minio", "url")))
                .credentials(
                        Objects.requireNonNull(
                                YamlConfigLoader.getConfigValue("minio", "username")),
                        Objects.requireNonNull(
                                YamlConfigLoader.getConfigValue("minio", "password")))
                .build();
    }

    /**
     * Uploads multiple artifact paths to a specified MinIO bucket.
     *
     * @param bucketName The bucket where artifacts should be uploaded
     * @param filePaths  List of relative file or folder paths under LOCAL_ARTIFACTS_DIRECTORY
     * @throws MinioException           if a MinIO-related error occurs
     * @throws IOException              if file access or path resolution fails
     * @throws NoSuchAlgorithmException if cryptographic algorithm is unavailable
     * @throws InvalidKeyException      if credentials are invalid
     */
    public void uploadArtifacts(String bucketName, List<String> filePaths)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty.");
        }

        if (minioClient == null) {
            throw new IllegalStateException("MinIO client is not initialized.");
        }

        boolean isExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        for (String relativePath : filePaths) {
            String fullPath = Constants.LOCAL_ARTIFACTS_DIRECTORY + "/" + relativePath;

            File file = new File(fullPath);

            if (!containsWildcard(fullPath) && !file.exists()) {
                throw new IOException("File or directory not found: " + fullPath);
            }
            try {
                if (file.isFile()) {
                    uploadFile(bucketName, file);
                } else if (file.isDirectory()) {
                    uploadDirectory(bucketName, file);
                } else {
                    processWildcard(bucketName, fullPath);
                }
            } catch (Exception e) {
                throw new IOException("Failed to upload path: " + fullPath, e);
            }
        }
    }

    /**
     * Uploads a single file to the specified MinIO bucket.
     *
     * @param bucketName The bucket name
     * @param file       The file to upload
     * @throws MinioException           if a MinIO-related error occurs
     * @throws IOException              if file access fails
     * @throws NoSuchAlgorithmException if cryptographic algorithm is unavailable
     * @throws InvalidKeyException      if credentials are invalid
     */
    private void uploadFile(String bucketName, File file)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Cannot upload non-existent file: " +
                    file.getAbsolutePath());
        }

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(file.getName())
                        .filename(file.getAbsolutePath())
                        .build());
    }

    /**
     * Recursively uploads all files in a directory to the specified MinIO bucket.
     *
     * @param bucketName The bucket name
     * @param directory  The directory to walk and upload
     * @throws IOException if directory traversal or file access fails
     */
    private void uploadDirectory(String bucketName, File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " +
                    directory.getAbsolutePath());
        }

        try (var paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            uploadFile(bucketName, path.toFile());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to upload file in directory: " +
                                    path, e);
                        }
                    });
        }
    }


    /**
     * Handles wildcard-based file uploads using glob patterns.
     *
     * @param bucketName The bucket name
     * @param pattern    The path with wildcard (e.g., artifacts/*.json)
     * @throws IOException if pattern matching or file access fails
     */
    private void processWildcard(String bucketName, String pattern) throws IOException {
        int lastSlashIndex = pattern.lastIndexOf(File.separator);
        String baseDir = ".";
        String globPattern = pattern;

        if (lastSlashIndex != -1) {
            baseDir = pattern.substring(0, lastSlashIndex);
            globPattern = pattern.substring(lastSlashIndex + 1);
        }

        Path basePath = Paths.get(baseDir);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);

        try (var paths = Files.walk(basePath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(basePath.relativize(path)))
                    .forEach(path -> {
                        try {
                            uploadFile(bucketName, path.toFile());
                        } catch (Exception e) {
                            throw new RuntimeException("Wildcard upload failed for file: " +
                                    path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IOException("Failed to walk through wildcard path: " +
                    basePath + "/" + globPattern, e);
        }
    }

    /**
     * Manually sets a custom MinIO client (for testing or reconfiguration).
     *
     * @param minioClient The custom {@link MinioClient} to use.
     */

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Utility method to check if a given path contains wildcard characters.
     *
     * @param path The path to inspect
     * @return {@code true} if the path includes '*' or '?', otherwise {@code false}
     */
    private boolean containsWildcard(String path) {
        return path.contains("*") || path.contains("?");
    }
}
