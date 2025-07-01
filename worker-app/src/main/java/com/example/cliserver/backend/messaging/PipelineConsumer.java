package com.example.cliserver.backend.messaging;

import static com.example.cliserver.backend.utils.Constants.PIPELINE_RUN_JSON_KEY;
import static com.example.cliserver.backend.utils.Constants.QUEUE_NAME;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDaoFactory;
import com.example.cliserver.backend.runCommand.PipelineRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.utils.YamlConfigLoader;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Worker that consumes pipeline execution requests from RabbitMQ and executes them
 * using the existing PipelineRunner.
 */
public class PipelineConsumer {
    private final ConnectionFactory factory;
    private final PipelineRunner pipelineRunner;

    private final ObjectMapper objectMapper;

    /**
     * Constructs a PipelineConsumer with the specified PipelineRunner.
     *
     */
    public PipelineConsumer() {
        this.pipelineRunner = new PipelineRunner(PipelineRunsDaoFactory.getInstance());
        this.factory = new ConnectionFactory();
        this.objectMapper = new ObjectMapper();
        this.factory.setHost(YamlConfigLoader.getConfigValue("rabbitmq", "host"));
    }

    /**
     * Starts the consumer, connecting to RabbitMQ and waiting for pipeline execution requests.
     */
    public void start() {
        try {

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1);

            System.out.println("Pipeline consumer started and waiting for messages...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Received pipeline execution request: " + message);

                try {
                    // Parse message
                    JSONObject json = new JSONObject(message);
                    String pipelineRunJson = json.getString(PIPELINE_RUN_JSON_KEY);


                    PipelineRun pipelineRun = objectMapper.readValue(
                        pipelineRunJson, PipelineRun.class
                    );

                    // Run the pipeline without logging (on worker application)
                    pipelineRunner.runPipeline(pipelineRun);

                    // Acknowledge a message
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());

                    // Reject the message (don't requeue)
                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            // Start consuming messages
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            System.err.println("Error starting pipeline consumer: " + e.getMessage());
        }
    }

    /**
     * Initiates shutdown of the executor service.
     * <p>
     * The method first attempts to shut down the executor by calling {@code shutdown()} and waits
     * for up to 30 seconds for all tasks to finish. If the tasks do not complete within the
     * specified time, it forcefully shuts down the executor by calling {@code shutdownNow()}.
     * </p>
     * <p>
     * If the current thread is interrupted while waiting for tasks to terminate, the executor is
     * forcefully shut down, and the thread's interrupt status is preserved.
     * </p>
     */
    public void shutdownExecutor() {
        pipelineRunner.shutdownExecutor();
    }
}
