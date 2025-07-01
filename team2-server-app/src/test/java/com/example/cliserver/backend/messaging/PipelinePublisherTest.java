package com.example.cliserver.backend.messaging;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.utils.YamlConfigLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static com.example.cliserver.backend.utils.Constants.PIPELINE_RUN_JSON_KEY;
import static com.example.cliserver.backend.utils.Constants.QUEUE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelinePublisherTest {

    @Mock
    private PipelineRunsDao pipelineRunsDao;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection connection;

    @Mock
    private Channel channel;

    private PipelinePublisher pipelinePublisher;
    private PipelineRun pipelineRun;

    @BeforeEach
    void setUp() throws IOException, TimeoutException {
        // Create a test pipeline run
        pipelineRun = new PipelineRun();
        pipelineRun.setPipelineName("test-pipeline");
        pipelineRun.setRunNumber(123);
        pipelineRun.setRepo("https://github.com/example/repo");
    }

    @Test
    void testPublishPipelineRun_Success() throws Exception {
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        try (MockedStatic<YamlConfigLoader> mockedStatic = Mockito.mockStatic(YamlConfigLoader.class)) {
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("rabbitmq", "host"))
                .thenReturn("localhost");
            pipelinePublisher = new PipelinePublisher(pipelineRunsDao);

            java.lang.reflect.Field factoryField = PipelinePublisher.class.getDeclaredField("factory");
            factoryField.setAccessible(true);
            factoryField.set(pipelinePublisher, connectionFactory);
            String result = pipelinePublisher.publishPipelineRun(pipelineRun);
            verify(channel).queueDeclare(eq(QUEUE_NAME), eq(true), eq(false), eq(false), eq(null));
            verify(channel).basicPublish(eq(""), eq(QUEUE_NAME), eq(null), any(byte[].class));
            assertEquals("test-pipeline run: 123", result);
        }
    }

    @Test
    void testPublishPipelineRun_ConnectionError() throws Exception {
        when(connectionFactory.newConnection()).thenThrow(new IOException("Connection refused"));
        try (MockedStatic<YamlConfigLoader> mockedStatic = Mockito.mockStatic(YamlConfigLoader.class)) {
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("rabbitmq", "host"))
                .thenReturn("localhost");
            pipelinePublisher = new PipelinePublisher(pipelineRunsDao);
            java.lang.reflect.Field factoryField = PipelinePublisher.class.getDeclaredField("factory");
            factoryField.setAccessible(true);
            factoryField.set(pipelinePublisher, connectionFactory);
            String result = pipelinePublisher.publishPipelineRun(pipelineRun);
            assertTrue(result.startsWith("Error publishing pipeline run:"), "Error message should start with the expected prefix");
            assertTrue(result.contains("Connection refused"), "Error message should contain the cause");
        }
    }

    @Test
    void testPublishPipelineRun_ChannelError() throws Exception {
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        when(channel.queueDeclare(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any()))
            .thenThrow(new IOException("Channel error"));

        try (MockedStatic<YamlConfigLoader> mockedStatic = Mockito.mockStatic(YamlConfigLoader.class)) {
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("rabbitmq", "host"))
                .thenReturn("localhost");
            pipelinePublisher = new PipelinePublisher(pipelineRunsDao);
            java.lang.reflect.Field factoryField = PipelinePublisher.class.getDeclaredField("factory");
            factoryField.setAccessible(true);
            factoryField.set(pipelinePublisher, connectionFactory);
            String result = pipelinePublisher.publishPipelineRun(pipelineRun);
            verify(connection).createChannel();
            assertTrue(result.startsWith("Error publishing pipeline run:"), "Error message should start with the expected prefix");
            assertTrue(result.contains("Channel error"), "Error message should contain the cause");
        }
    }

    @Test
    void testPublishPipelineRun_MessageContentVerification() throws Exception {
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        try (MockedStatic<YamlConfigLoader> mockedStatic = Mockito.mockStatic(YamlConfigLoader.class)) {
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("rabbitmq", "host"))
                .thenReturn("localhost");
            pipelinePublisher = new PipelinePublisher(pipelineRunsDao);
            java.lang.reflect.Field factoryField = PipelinePublisher.class.getDeclaredField("factory");
            factoryField.setAccessible(true);
            factoryField.set(pipelinePublisher, connectionFactory);
            doAnswer(invocation -> {
                byte[] messageBytes = invocation.getArgument(3);
                String messageContent = new String(messageBytes, StandardCharsets.UTF_8);
                JSONObject messageJson = new JSONObject(messageContent);
                assertTrue(messageJson.has(PIPELINE_RUN_JSON_KEY), "Message should contain the pipeline run key");

                String pipelineRunJson = messageJson.getString(PIPELINE_RUN_JSON_KEY);
                JSONObject pipelineRunObject = new JSONObject(pipelineRunJson);

                assertEquals("test-pipeline", pipelineRunObject.getString("pipelineName"), "Pipeline name should match");
                assertEquals(123, pipelineRunObject.getInt("runNumber"), "Run number should match");
                assertEquals("https://github.com/example/repo", pipelineRunObject.getString("repo"), "Repo should match");

                return null;
            }).when(channel).basicPublish(anyString(), anyString(), any(), any(byte[].class));

            pipelinePublisher.publishPipelineRun(pipelineRun);
            verify(channel).basicPublish(anyString(), anyString(), any(), any(byte[].class));
        }
    }
}