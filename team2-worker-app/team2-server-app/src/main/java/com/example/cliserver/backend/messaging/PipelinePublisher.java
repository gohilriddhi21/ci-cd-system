package com.example.cliserver.backend.messaging;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.utils.YamlConfigLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.example.cliserver.backend.utils.Constants.*;

/**
 * Responsible for validating pipeline configurations and publishing pipeline
 * execution requests to the RabbitMQ message queue.
 */
public class PipelinePublisher {
    private final ConnectionFactory factory;
    public final PipelineRunsDao pipelineRunsDao;

    /**
     * Creates a new PipelinePublisher with the specified DAO.
     *
     * @param pipelineRunsDao the DAO for pipeline runs
     */

    public PipelinePublisher(PipelineRunsDao pipelineRunsDao) {
        this.pipelineRunsDao = pipelineRunsDao;
        this.factory = new ConnectionFactory();
        this.factory.setHost(YamlConfigLoader.getConfigValue("rabbitmq", "host"));
    }

    /**
     * Validates a pipeline configuration, creates a new run record, and
     * publishes a message to the queue for execution.
     *
     * @param pipelineRunReport the pipeline run report
     * @return a message indicating success with run number, or an error message
     */
    public String publishPipelineRun(PipelineRun pipelineRunReport) {

        // Send a message to queue - fire and forget
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            JSONObject pipelineRunJson = new JSONObject(pipelineRunReport);

            // Create a message with necessary information
            Map<String, Object> message = new HashMap<>();
            message.put(PIPELINE_RUN_JSON_KEY, pipelineRunJson.toString());

            String messageBody = new JSONObject(message).toString();
            channel.basicPublish(
                    "", QUEUE_NAME, null, messageBody.getBytes(StandardCharsets.UTF_8)
            );

            return pipelineRunReport.getPipelineName() + " run: "
                    + pipelineRunReport.getRunNumber();
        } catch (Exception e) {
            return "Error publishing pipeline run: " + e.getMessage();
        }

    }
}