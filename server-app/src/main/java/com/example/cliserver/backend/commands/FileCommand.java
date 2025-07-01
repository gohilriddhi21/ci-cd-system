package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.utils.Constants;
import com.example.cliserver.backend.utils.GitUtils;

import java.io.File;

/**
 * This class provides functionality to check
 * for the existence of a file in the
 * `.pipeline` directory.
 */
public class FileCommand {
    /**
     * Checks if the specified file exists in the `.pipeline` directory.
     *
     * @param filename The name of the file to check.
     * @param repo The url of the repository for the file, or null if local
     * @param branch The branch to check out for the given repo
     * @param commit The commit hash to check out for the given repo
     * @return A string message indicating whether the file is present or a not
     *
     */
    public static String checkFile(String filename, String repo, String branch,
                                   String commit) {
        String localDir;
        String result;
        if (repo != null) {
            String cloneToDirectory = GitUtils.getRepoDirectoryFromURL(repo);
            GitUtils.cloneRemoteRepo(cloneToDirectory, repo, branch, commit);
            localDir = cloneToDirectory + Constants.DIRECTORY;
        } else {
            localDir = Constants.DIRECTORY;
        }
        File file = new File(localDir, filename);
        if (file.exists()) {
            result = "\nFile present: " + file.getAbsolutePath();
        } else {
            result = "\nFile not found in .pipelines directory.";
        }
        if (repo != null) {
            GitUtils.cleanUpRemoteDirectory(repo);
        }
        return result;
    }
}