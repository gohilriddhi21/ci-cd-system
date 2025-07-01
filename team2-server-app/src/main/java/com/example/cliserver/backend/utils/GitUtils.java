package com.example.cliserver.backend.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Utility class for performing Git operations such as cloning, checking out branches/commits,
 * verifying commit or branch existence, and cleaning up cloned repositories.
 * <p>
 * This class makes use of JGit for programmatically interacting with Git repositories.
 */
public class GitUtils {
    /**
     * default constructor
     */
    private GitUtils(){}
    /**
     * Clones a remote Git repository into a specified local directory.
     * This method catches exceptions related to cloning and prints the error stack trace.
     *
     * @param localDir The path to the local directory where the repository will be cloned
     * @param repoPath The URL of the remote Git repository to clone
     * @param branch   The branch to use for the remote repo
     * @param commitHash The commit hash to use for the remote repo
     */
    public static void cloneRemoteRepo(String localDir, String repoPath, String branch,
                                       String commitHash) {
        try {
            cloneRepository(repoPath, localDir, branch, commitHash);
        } catch (GitAPIException e) {
            System.out.println("Error cloning remote repo: " + repoPath + ": " + e.getMessage());
        }
    }

    /**
     * Clone a remote repository to a local directory
     *
     * @param remoteRepoUrl The URL of the remote repository
     * @param branch The branch to checkout for the given repo
     * @param commitHash The commit hash to checkout for the given repo
     * @param localDir      The local directory to clone into
     * @throws GitAPIException if an error occurs during the cloning process
     */
    private static void cloneRepository(String remoteRepoUrl, String localDir,
                                        String branch, String commitHash)
            throws GitAPIException {
        try {
            Git git = Git.cloneRepository()
                    .setURI(remoteRepoUrl)
                    .setDirectory(new File(localDir))
                    .call();
            if (branch != null) {
                if (branchExists(git, branch)) {
                    git.checkout().setName(Constants.REMOTE_BRANCH_PREFIX + branch).call();
                } else {
                    cleanUpRemoteDirectory(remoteRepoUrl);
                    throw new IOException("Invalid branch name: " + branch);
                }
            }
            if (commitHash != null) {
                if (commitExists(git, commitHash)) {
                    git.checkout().setAllPaths(true).setStartPoint(commitHash).call();
                } else {
                    cleanUpRemoteDirectory(remoteRepoUrl);
                    throw new IOException("Invalid commit hash: " + commitHash);
                }
            }
        } catch (IOException e) {
            System.out.println("Error cloning repo: " + remoteRepoUrl + " " + e.getMessage());
        }
    }

    /**
     * Checks that the given branch exists in the given git repo
     *
     * @param git        the git object for the repo
     * @param branchName the branch name to check
     * @return true if the branch exists in that repo, else false
     */
    private static boolean branchExists(Git git, String branchName) {
        try {
            Ref ref = git.getRepository().findRef(Constants.REMOTE_BRANCH_PREFIX + branchName);
            return ref != null;
        } catch (IOException e) {
            System.out.println("Error pulling branch: " + branchName + " in remote repo. "
                    + e.getMessage());
            return false;
        }
    }

    /**
     * Checks that the given commit exists in the given git repo
     *
     * @param git        the git object for the repo
     * @param commitHash the commit hash to check
     * @return true if the commit exists in that repo, else false
     */
    private static boolean commitExists(Git git, String commitHash) {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            ObjectId commitId = ObjectId.fromString(commitHash);
            RevCommit commit = revWalk.parseCommit(commitId);
            return commit != null;
        } catch (Exception e) {
            System.out.println("Error pulling commit: " + commitHash + " in remote repo. "
                    + e.getMessage());
            return false;
        }
    }

    /**
     * Cleans up (deletes) the directory of a cloned repository from the local system.
     * It first checks if the directory exists and is valid before attempting to delete it.
     *
     * @param repo The path to the repo
     */
    public static void cleanUpRemoteDirectory(String repo) {
        String localDir = getRepoDirectoryFromURL(repo);
        File directory = new File(localDir);
        if (directory.exists() && directory.isDirectory()) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                System.err.println("Error cleaning up local directory: " + localDir + " for repo: "
                        + repo + ": " + e.getMessage());
            }
        }
    }

    /**
     * Extracts the repository directory name from the given URL.
     * The URL must follow the format "https://github.com/user/repo.git".
     *
     * @param repoPath The URL of the Git repository
     * @return The repository name (e.g., "user/repo"), or null if the URL is invalid
     */
    public static String getRepoDirectoryFromURL(String repoPath) {
        try {
            URI uri = new URI(repoPath);

            String path = uri.getPath();

            String userAndRepo = path.substring(1); // Remove the leading '/'
            if (userAndRepo.endsWith(".git")) {
                userAndRepo = userAndRepo.substring(0, userAndRepo.length() - 4); // Remove ".git"
            }
            // Replace so we do not create a nested directory
            userAndRepo = userAndRepo.replace("/", "-");

            return Constants.REMOTE_DIRECTORY + userAndRepo + "/";
        } catch (URISyntaxException e) {
            System.err.println("Invalid URL: " + e.getMessage());
            return null;
        }
    }
}
