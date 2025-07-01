package com.example.cliserver.backend.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;

class GitUtilsTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        outContent.reset();
        errContent.reset();
    }

    @Test
    void testGetRepoDirectoryFromURL_valid() {
        String repoURL = "https://github.com/user/repo.git";
        String expected = Constants.REMOTE_DIRECTORY + "user-repo" + "/";
        String result = GitUtils.getRepoDirectoryFromURL(repoURL);
        assertEquals(expected, result);
    }

    @Test
    void testGetRepoDirectoryFromURL_invalid() {
        String repoURL = "http://inva lid/repo.git";
        String result = GitUtils.getRepoDirectoryFromURL(repoURL);
        assertNull(result);
        String errOutput = errContent.toString().trim();
        assertTrue(errOutput.contains("Invalid URL"), "Expected error message about invalid URL");
    }

    @Test
    void testCleanUpRemoteDirectory(@TempDir Path tempDir) throws IOException, URISyntaxException {
        String repoURL = "https://github.com/user/repo.git";
        String dirPath = GitUtils.getRepoDirectoryFromURL(repoURL);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            assertTrue(dir.mkdirs(), "Directory should be created for cleanup test");
        }
        assertTrue(dir.exists());
        GitUtils.cleanUpRemoteDirectory(repoURL);
        assertFalse(dir.exists(), "Directory should be deleted by cleanUpRemoteDirectory");
    }

    private Git createRemoteRepository(Path tempDir, String branchName) throws Exception {
        File repoDir = tempDir.resolve("remoteRepo").toFile();
        Git git = Git.init().setDirectory(repoDir).call();
        File testFile = new File(repoDir, "test.txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Hello, Git!");
        }
        git.add().addFilepattern("test.txt").call();
        RevCommit commit = git.commit().setMessage("Initial commit").call();
        if (branchName != null && !branchName.isEmpty() &&
            !branchName.equals("master") && !branchName.equals("main")) {
            git.branchCreate().setName(branchName).call();
        }
        return git;
    }

    @Test
    void testCloneRemoteRepo_success(@TempDir Path tempDir) throws Exception {
        String branch = "testbranch";
        Git remoteGit = createRemoteRepository(tempDir, branch);
        remoteGit.checkout().setName(branch).call();
        RevCommit commit = remoteGit.log().setMaxCount(1).call().iterator().next();
        String commitHash = commit.getId().getName();
        File remoteRepoDir = remoteGit.getRepository().getDirectory().getParentFile();
        String remoteRepoURI = remoteRepoDir.toURI().toString();
        File localCloneDir = tempDir.resolve("localClone").toFile();
        GitUtils.cloneRemoteRepo(localCloneDir.getAbsolutePath(), remoteRepoURI, branch, commitHash);
        File gitDir = new File(localCloneDir, ".git");
        assertTrue(localCloneDir.exists() && localCloneDir.isDirectory());
        assertTrue(gitDir.exists() && gitDir.isDirectory());
        try (Git clonedGit = Git.open(localCloneDir)) {
            String headCommit = clonedGit.getRepository().resolve("HEAD").getName();
            assertEquals(commitHash, headCommit);
        }
        remoteGit.close();
    }

    @Test
    void testCloneRemoteRepo_invalidBranch(@TempDir Path tempDir) throws Exception {
        Git remoteGit = createRemoteRepository(tempDir, null);
        File remoteRepoDir = remoteGit.getRepository().getDirectory().getParentFile();
        String remoteRepoURI = remoteRepoDir.toURI().toString();
        File localCloneDir = tempDir.resolve("localCloneInvalidBranch").toFile();
        String invalidBranch = "nonexistent";
        GitUtils.cloneRemoteRepo(localCloneDir.getAbsolutePath(), remoteRepoURI, invalidBranch, null);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Invalid branch name: " + invalidBranch),
            "Expected error message for invalid branch");
        String cleanupDirName = GitUtils.getRepoDirectoryFromURL(remoteRepoURI);
        File cleanupDir = new File(cleanupDirName);
        assertFalse(cleanupDir.exists(), "Cleanup directory should not exist after invalid branch");
        remoteGit.close();
    }

    @Test
    void testCloneRemoteRepo_invalidCommit(@TempDir Path tempDir) throws Exception {
        Git remoteGit = createRemoteRepository(tempDir, null);
        File remoteRepoDir = remoteGit.getRepository().getDirectory().getParentFile();
        String remoteRepoURI = remoteRepoDir.toURI().toString();
        File localCloneDir = tempDir.resolve("localCloneInvalidCommit").toFile();
        String invalidCommit = "abcdef1234567890";
        GitUtils.cloneRemoteRepo(localCloneDir.getAbsolutePath(), remoteRepoURI, null, invalidCommit);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Invalid commit hash: " + invalidCommit),
            "Expected error message for invalid commit hash");
        String cleanupDirName = GitUtils.getRepoDirectoryFromURL(remoteRepoURI);
        File cleanupDir = new File(cleanupDirName);
        assertFalse(cleanupDir.exists(), "Cleanup directory should not exist after invalid commit");
        remoteGit.close();
    }
}
