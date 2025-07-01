package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.utils.Constants;
import com.example.cliserver.backend.utils.GitUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

public class FileCommandTest {

    @TempDir
    Path tempDir;

    private String originalDirectory;

    @BeforeEach
    public void setUp() throws Exception {
        // Save original value of Constants.DIRECTORY and override it.
        Field directoryField = Constants.class.getDeclaredField("DIRECTORY");
        directoryField.setAccessible(true);
        originalDirectory = (String) directoryField.get(null);
        // Override DIRECTORY to the temporary directory absolute path.
        directoryField.set(null, tempDir.toFile().getAbsolutePath());
    }

    @AfterEach
    public void tearDown() throws Exception {
        Field directoryField = Constants.class.getDeclaredField("DIRECTORY");
        directoryField.setAccessible(true);
        directoryField.set(null, originalDirectory);
    }

    @Test
    public void testCheckFileLocalExists() throws Exception {
        String filename = "test.yaml";
        // Create the file in the overridden local directory (tempDir).
        File file = new File(tempDir.toFile(), filename);
        boolean created = file.createNewFile();
        if (!created) {
            throw new Exception("Failed to create test file: " + file.getAbsolutePath());
        }

        String result = FileCommand.checkFile(filename, null, null, null);

        assertTrue(result.contains("File present:"), "Expected a file present message");
        assertTrue(result.contains(file.getAbsolutePath()), "Expected the message to contain the file absolute path");
    }

    @Test
    public void testCheckFileLocalNotExists() {
        String filename = "nonexistent.yaml";
        String result = FileCommand.checkFile(filename, null, null, null);
        assertEquals("\nFile not found in .pipelines directory.", result);
    }

    @Test
    public void testCheckFileRemoteExists() throws Exception {
        String filename = "remote.yaml";
        String repo = "http://example.com/repo.git";
        String branch = "master";
        String commit = "commitHash";

        try (MockedStatic<GitUtils> gitUtilsMock = Mockito.mockStatic(GitUtils.class)) {
            gitUtilsMock.when(() -> GitUtils.getRepoDirectoryFromURL(repo)).thenReturn("");
            gitUtilsMock.when(() -> GitUtils.cloneRemoteRepo("", repo, branch, commit)).thenAnswer(invocation -> null);
            gitUtilsMock.when(() -> GitUtils.cleanUpRemoteDirectory(repo)).thenAnswer(invocation -> null);

            File file = new File(tempDir.toFile(), filename);
            boolean created = file.createNewFile();
            if (!created) {
                throw new Exception("Failed to create test file: " + file.getAbsolutePath());
            }

            String result = FileCommand.checkFile(filename, repo, branch, commit);
            assertTrue(result.contains("File present:"), "Expected a file present message for remote case");
            assertTrue(result.contains(file.getAbsolutePath()), "Expected the message to contain the file absolute path");

            gitUtilsMock.verify(() -> GitUtils.cloneRemoteRepo("", repo, branch, commit), times(1));
            gitUtilsMock.verify(() -> GitUtils.cleanUpRemoteDirectory(repo), times(1));
        }
    }

    @Test
    public void testCheckFileRemoteNotExists() {
        String filename = "nonexistent_remote.yaml";
        String repo = "http://example.com/repo.git";
        String branch = "master";
        String commit = "commitHash";

        try (MockedStatic<GitUtils> gitUtilsMock = Mockito.mockStatic(GitUtils.class)) {
            gitUtilsMock.when(() -> GitUtils.getRepoDirectoryFromURL(repo)).thenReturn("");
            gitUtilsMock.when(() -> GitUtils.cloneRemoteRepo("", repo, branch, commit)).thenAnswer(invocation -> null);
            gitUtilsMock.when(() -> GitUtils.cleanUpRemoteDirectory(repo)).thenAnswer(invocation -> null);

            File file = new File(tempDir.toFile(), filename);
            if (file.exists()) {
                file.delete();
            }

            String result = FileCommand.checkFile(filename, repo, branch, commit);
            assertEquals("\nFile not found in .pipelines directory.", result);

            gitUtilsMock.verify(() -> GitUtils.cloneRemoteRepo("", repo, branch, commit), times(1));
            gitUtilsMock.verify(() -> GitUtils.cleanUpRemoteDirectory(repo), times(1));
        }
    }
}
