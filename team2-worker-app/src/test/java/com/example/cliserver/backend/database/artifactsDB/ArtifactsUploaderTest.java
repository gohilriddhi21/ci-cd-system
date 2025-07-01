package com.example.cliserver.backend.database.artifactsDB;

import com.example.cliserver.backend.utils.Constants;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ArtifactsUploaderTest {
    @TempDir
    Path tempDir;
    private ArtifactsUploader uploader;
    private MinioClient mockMinio;

    @BeforeEach
    void setUp() {
        Constants.LOCAL_ARTIFACTS_DIRECTORY = tempDir.toString();

        mockMinio = mock(MinioClient.class);
        uploader = new ArtifactsUploader();
        uploader.setMinioClient(mockMinio);
    }

    @Test
    void testUploadSingleFile() throws Exception {
        File file = new File(tempDir.toFile(), "single.txt");
        assertTrue(file.createNewFile());
        file.deleteOnExit();

        when(mockMinio.bucketExists(any())).thenReturn(true);

        uploader.uploadArtifacts("test-bucket", List.of("single.txt"));

        verify(mockMinio).uploadObject(argThat(args ->
                args instanceof UploadObjectArgs &&
                        (args).object().equals("single.txt")
        ));
    }

    @Test
    void testUploadArtifacts_nullMinioClient() {
        uploader.setMinioClient(null);
        List<String> filePaths = Collections.singletonList("test.txt");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                uploader.uploadArtifacts("test-bucket", filePaths));

        assertEquals("MinIO client is not initialized.", exception.getMessage());
    }


    @Test
    void testUploadArtifacts_nullBucketName() {
        List<String> filePaths = Collections.singletonList("test.txt");
        assertThrows(IllegalArgumentException.class, () ->
                uploader.uploadArtifacts(null, filePaths));
    }

    @Test
    void testUploadArtifacts_emptyBucketName() {
        List<String> filePaths = Collections.singletonList("test.txt");
        assertThrows(IllegalArgumentException.class, () ->
                uploader.uploadArtifacts("", filePaths));
    }

    @Test
    void testUploadFile_exceptionHandled() throws Exception {
        File file = new File(tempDir.toFile(), "errorFile.txt");
        assertTrue(file.createNewFile());
        file.deleteOnExit();

        doThrow(new RuntimeException("Upload failed")).when(mockMinio).uploadObject(any());
        when(mockMinio.bucketExists(any())).thenReturn(true);

        IOException ex = assertThrows(IOException.class, () ->
                uploader.uploadArtifacts("bucket", List.of("errorFile.txt"))
        );

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof RuntimeException);
        assertEquals("Upload failed", ex.getCause().getMessage());
    }


    @Test
    void testUploadDirectory() throws Exception {
        File dir = new File(tempDir.toFile(), "uploadDir");
        assertTrue(dir.mkdirs());

        File file1 = new File(dir, "fileA.txt");
        File file2 = new File(dir, "fileB.txt");
        assertTrue(file1.createNewFile());
        assertTrue(file2.createNewFile());
        file1.deleteOnExit();
        file2.deleteOnExit();

        when(mockMinio.bucketExists(any())).thenReturn(false);
        doNothing().when(mockMinio).makeBucket(any());

        uploader.uploadArtifacts("test-bucket", List.of("uploadDir"));

        verify(mockMinio).makeBucket(any());
        verify(mockMinio, times(2)).uploadObject(any());
    }

    @Test
    void testUploadWildcardPattern() throws Exception {
        File wildcardDir = new File(tempDir.toFile(), "wild");
        assertTrue(wildcardDir.mkdirs());

        File file1 = new File(wildcardDir, "a.txt");
        File file2 = new File(wildcardDir, "b.txt");
        File file3 = new File(wildcardDir, "not_included.md");

        assertTrue(file1.createNewFile());
        assertTrue(file2.createNewFile());
        assertTrue(file3.createNewFile());

        file1.deleteOnExit();
        file2.deleteOnExit();
        file3.deleteOnExit();

        Constants.LOCAL_ARTIFACTS_DIRECTORY = tempDir.toString();
        when(mockMinio.bucketExists(any())).thenReturn(true);

        uploader.uploadArtifacts("test-bucket", List.of("wild/*.txt"));

        verify(mockMinio, times(2)).uploadObject(any());
    }


    @Test
    void testCreatesBucketIfNotExists() throws Exception {
        File file = new File(tempDir.toFile(), "createBucket.txt");
        assertTrue(file.createNewFile());
        file.deleteOnExit();

        when(mockMinio.bucketExists(any())).thenReturn(false);

        uploader.uploadArtifacts("new-bucket", List.of("createBucket.txt"));

        verify(mockMinio).makeBucket(argThat(args ->
                args != null && args.bucket().equals("new-bucket")
        ));

        verify(mockMinio).uploadObject(argThat(args ->
                args instanceof UploadObjectArgs &&
                        (args).object().equals("createBucket.txt")
        ));
    }

    @Test
    void testUploadArtifacts_withInvalidWildcardPath() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        when(mockMinio.bucketExists(any())).thenReturn(true);

        String invalidPattern = "non_existing_dir/**/*.txt";

        assertThrows(IOException.class, () ->
                uploader.uploadArtifacts("bucket", List.of(invalidPattern))
        );
    }

    @Test
    void testUploadDirectory_withFileUploadException() throws Exception {
        File dir = new File(tempDir.toFile(), "errorDir");
        assertTrue(dir.mkdirs());

        File file = new File(dir, "file.txt");
        assertTrue(file.createNewFile());
        file.deleteOnExit();

        when(mockMinio.bucketExists(any())).thenReturn(true);
        doThrow(new RuntimeException("Upload failed")).when(mockMinio).uploadObject(any());

        IOException ex = assertThrows(IOException.class, () ->
                uploader.uploadArtifacts("bucket", List.of("errorDir"))
        );

        assertTrue(ex.getCause().getMessage().contains("Failed to upload file in directory:"));
    }
    @Test
    void testWildcardUpload_throwsRuntimeException() throws Exception {
        File wildcardDir = new File(tempDir.toFile(), "wildcard");
        assertTrue(wildcardDir.mkdirs());

        File file = new File(wildcardDir, "match.txt");
        assertTrue(file.createNewFile());

        Constants.LOCAL_ARTIFACTS_DIRECTORY = tempDir.toString();
        when(mockMinio.bucketExists(any())).thenReturn(true);

        doThrow(new RuntimeException("Upload failed")).when(mockMinio).uploadObject(any());

        IOException ex = assertThrows(IOException.class, () ->
                uploader.uploadArtifacts("bucket", List.of("wildcard/*.txt")));

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Wildcard upload failed for file"));
    }


    @Test
    void testUploadFile_nonExistent() {
        IOException ex = assertThrows(IOException.class, () ->
                uploader.uploadArtifacts("bucket", List.of("ghost.txt")));

        assertTrue(ex.getMessage().contains("File or directory not found"));
    }

    @Test
    void testUploadArtifacts_nonWildcardMissingFile() {
        String missing = "non_existing_file.txt";

        IOException ex = assertThrows(IOException.class, () ->
                uploader.uploadArtifacts("bucket", List.of(missing)));

        assertTrue(ex.getMessage().contains("File or directory not found"));
    }
}