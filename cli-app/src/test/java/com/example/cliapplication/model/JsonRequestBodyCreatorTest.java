package com.example.cliapplication.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonRequestBodyCreatorTest {

    private final JsonRequestBodyCreator creator = new JsonRequestBodyCreator();

    @Test
    void testCreateRequestBody_basicCommand() {
        String command = "run --filename correct.yaml --vv";
        String expected = "{\"Command\":\"run --filename correct.yaml --vv\"}";
        assertEquals(expected, creator.createRequestBody(command));
    }

    @Test
    void testCreateRequestBody_checkCommand() {
        String command = "-c -f correct.yaml";
        String expected = "{\"Command\":\"-c -f correct.yaml\"}";
        assertEquals(expected, creator.createRequestBody(command));
    }

    @Test
    void testCreateRequestBody_statusCommand() {
        String command = "status -f correct.yaml -rn 10";
        String expected = "{\"Command\":\"status -f correct.yaml -rn 10\"}";
        assertEquals(expected, creator.createRequestBody(command));
    }

    @Test
    void testCreateRequestBody_dryRunCommand() {
        String command = "-f correct.yaml";
        String expected = "{\"Command\":\"-f correct.yaml\"}";
        assertEquals(expected, creator.createRequestBody(command));
    }

    @Test
    void testCreateRequestBody_nullCommand_shouldThrowException() {
        String command = "";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            creator.createRequestBody(command);
        });
        assertEquals("Arguments must not be null or empty.", exception.getMessage());
    }

    @Test
    void createRequestBody_shouldThrowIllegalArgumentException_whenArgumentsAreBlank() {
        JsonRequestBodyCreator creator = new JsonRequestBodyCreator();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            creator.createRequestBody("   ");
        });
        assertEquals("Arguments must not be null or empty.", exception.getMessage());
    }
}