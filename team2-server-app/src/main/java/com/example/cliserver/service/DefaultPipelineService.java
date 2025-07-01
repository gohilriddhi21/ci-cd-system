package com.example.cliserver.service;

import com.example.cliserver.backend.commands.*;
import com.example.cliserver.backend.commands.runCommand.RunCommand;
import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.database.mongoDB.PipelineRunsDaoFactory;
import com.example.cliserver.backend.model.PipelineRequestParameters;
import com.example.cliserver.backend.utils.Constants;
import org.json.JSONObject;

/**
 * Default implementation of the PipelineService interface.
 */
public class DefaultPipelineService implements PipelineService {

    /**
     * DAO for pipeline run reports
     */
    public final PipelineRunsDao pipelineRunsDao;

    /**
     * Constructs a DefaultPipelineService with the given DAO.
     *
     */
    public DefaultPipelineService() {
        // Initialize DAO from factory - this will use CONFIG_FILE_PATH internally
        this.pipelineRunsDao = PipelineRunsDaoFactory.getInstance();
    }

    @Override
    public String validateConfiguration(PipelineRequestParameters params) {
        String filename = params.getFilename();
        String repo = params.getRepo();
        String branch = params.getBranch();
        String commit = params.getCommit();
        CheckCommand checkCommand = new CheckCommand();
        return checkCommand.validateYaml(Constants.DIRECTORY + filename, repo, branch,
                commit);
    }

    @Override
    public String runPipelineLocally(PipelineRequestParameters params) {
        String filename = params.getFilename();
        String repo = params.getRepo();
        String branch = params.getBranch();
        String commit = params.getCommit();
        Boolean verboseLogging = params.getVerboseLogging();

        String filePath = Constants.DIRECTORY + filename;
        RunCommand runCommand = new RunCommand(this.pipelineRunsDao);
        return runCommand.run(filePath, repo, branch, commit, verboseLogging);
    }

    @Override
    public String performDryRun(PipelineRequestParameters params) {
        String filename = params.getFilename();
        String repo = params.getRepo();
        String branch = params.getBranch();
        String commit = params.getCommit();
        return DryRunCommand.runDry(Constants.DIRECTORY + filename, repo, branch, commit);
    }

    @Override
    public String checkFileExists(PipelineRequestParameters params) {
        String filename = params.getFilename();
        String repo = params.getRepo();
        String branch = params.getBranch();
        String commit = params.getCommit();
        return FileCommand.checkFile(filename, repo, branch, commit);
    }

    @Override
    public JSONObject generateReport(PipelineRequestParameters params) {
        ReportCommand reportGenerator = new ReportCommand(pipelineRunsDao);
        return reportGenerator.generateReport(
                params.getPipelineName(),
                params.getStage(),
                params.getJob(),
                params.getRunNumber(),
                params.getRepo(),
                params.getFormat()
        );
    }

    @Override
    public String printPipelineStatus(PipelineRequestParameters params) {

        String filePath = params.getFilename() != null ?
                Constants.DIRECTORY + params.getFilename() :
                "";

        Integer runNumber = params.getRunNumber() != null ?
                Integer.parseInt(params.getRunNumber()) :
                null;

        String branch = params.getBranch();
        String commit = params.getCommit();

        StatusCommand statusCommand = new StatusCommand(pipelineRunsDao);
        return statusCommand.printPipelineStatus(
            params.getRepo(), branch, commit, filePath, runNumber
        );
    }
}