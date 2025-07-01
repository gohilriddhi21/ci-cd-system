package com.example.cliserver.backend.controller;

import com.example.cliserver.cli.CommandLineHandler;
import com.example.cliserver.controller.SseController;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SseControllerTest {

    private SseController sseController;

    @BeforeEach
    void setUp() {
        sseController = new SseController();
        setCurrentEmitter(null);
    }

    @AfterEach
    void tearDown() {
        setCurrentEmitter(null);
    }

    /**
     * Helper method to set the private static field "currentEmitter" via reflection.
     */
    private void setCurrentEmitter(SseEmitter emitter) {
        try {
            Field field = SseController.class.getDeclaredField("currentEmitter");
            field.setAccessible(true);
            field.set(null, emitter);
        } catch (Exception e) {
            throw new RuntimeException("Could not set currentEmitter", e);
        }
    }

    /**
     * Dummy SseEventBuilder that stores its data so that its toString() includes it.
     */
    public static class DummySseEventBuilder implements SseEventBuilder {
        private String data;

        @Override
        public SseEventBuilder data(Object data) {
            this.data = data == null ? "" : data.toString();
            return this;
        }

        @Override
        public SseEventBuilder data(Object object, MediaType mediaType) {
            return this;
        }

        @Override
        public Set<DataWithMediaType> build() {
            return new HashSet<>();
        }

        @Override
        public SseEventBuilder name(String name) {
            return this;
        }

        @Override
        public SseEventBuilder reconnectTime(long reconnectTimeMillis) {
            return this;
        }

        @Override
        public SseEventBuilder comment(String comment) {
            return this;
        }

        @Override
        public SseEventBuilder id(String id) {
            return this;
        }

        @Override
        public String toString() {
            return "data=" + data;
        }
    }

    // -------------------- Tests for receiveInitialMessage --------------------

    @Test
    void testReceiveInitialMessage_withValidMessage() {
        String message = "Hello, world!";
        ResponseEntity<String> response = sseController.receiveInitialMessage(message);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Initial message received: " + message));
    }

    @Test
    void testReceiveInitialMessage_withNullMessage() {
        ResponseEntity<String> response = sseController.receiveInitialMessage(null);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Error: Please provide a 'message' in the payload."));
    }


    @Test
    void testStaticSendEvent() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        setCurrentEmitter(emitter);

        try (MockedStatic<SseEmitter> sseStatic = mockStatic(SseEmitter.class)) {
            DummySseEventBuilder dummyBuilder = new DummySseEventBuilder();
            sseStatic.when(SseEmitter::event).thenReturn(dummyBuilder);

            SseController.sendEvent("static test");

            ArgumentCaptor<SseEventBuilder> captor =
                    ArgumentCaptor.forClass(SseEventBuilder.class);
            verify(emitter).send(captor.capture());
        }
    }

    @Test
    void testStaticSendEventAndComplete_success() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        try (MockedStatic<SseEmitter> sseStatic = mockStatic(SseEmitter.class)) {
            DummySseEventBuilder dummyBuilder = new DummySseEventBuilder();
            sseStatic.when(SseEmitter::event).thenReturn(dummyBuilder);

            SseController.sendEventAndComplete(emitter, "complete test");

            ArgumentCaptor<SseEventBuilder> captor =
                    ArgumentCaptor.forClass(SseEventBuilder.class);
            verify(emitter).send(captor.capture());
            String eventStr = captor.getValue().toString();
            assertTrue(eventStr.contains("complete test"));
            verify(emitter).complete();
        }
    }

    @Test
    void testStaticSendEventAndComplete_failure() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("IO Error")).when(emitter).send(any(SseEventBuilder.class));

        try (MockedStatic<SseEmitter> sseStatic = mockStatic(SseEmitter.class)) {
            DummySseEventBuilder dummyBuilder = new DummySseEventBuilder();
            sseStatic.when(SseEmitter::event).thenReturn(dummyBuilder);

            SseController.sendEventAndComplete(emitter, "fail test");

            ArgumentCaptor<SseEventBuilder> captor =
                    ArgumentCaptor.forClass(SseEventBuilder.class);
            verify(emitter).send(captor.capture());
            verify(emitter).completeWithError(any(IOException.class));
            verify(emitter).complete();
        }
    }

    @Test
    void testReceiveRawCommand_noCommandProvided() throws Exception {
        try (MockedStatic<SseEmitter> sseStatic = mockStatic(SseEmitter.class)) {
            DummySseEventBuilder dummyBuilder = new DummySseEventBuilder();
            sseStatic.when(SseEmitter::event).thenReturn(dummyBuilder);

            SseEmitter emitter = sseController.receiveRawCommand(null);
            assertNotNull(emitter);

            TimeUnit.MILLISECONDS.sleep(200);
        }
    }

    @Test
    void testReceiveRawCommand_emptyCommandProvided() throws Exception {
        try (MockedStatic<SseEmitter> sseStatic = mockStatic(SseEmitter.class)) {
            DummySseEventBuilder dummyBuilder = new DummySseEventBuilder();
            sseStatic.when(SseEmitter::event).thenReturn(dummyBuilder);

            SseEmitter emitter = sseController.receiveRawCommand("   ");
            assertNotNull(emitter);

            TimeUnit.MILLISECONDS.sleep(200);
        }
    }

    @Test
    void testReceiveRawCommand_CommandHandlerThrows() throws Exception {
        try (
                MockedConstruction<CommandLineHandler> mockedConstruction =
                        mockConstruction(CommandLineHandler.class, (mock, context) -> {
                            doThrow(new RuntimeException("handler error")).when(mock).run();
                        });
                MockedStatic<SseEmitter> sseStatic = mockStatic(SseEmitter.class)
        ) {
            DummySseEventBuilder dummyBuilder = new DummySseEventBuilder();
            sseStatic.when(SseEmitter::event).thenReturn(dummyBuilder);

            SseEmitter emitter = sseController.receiveRawCommand("run -f file.yml");
            assertNotNull(emitter);

            TimeUnit.MILLISECONDS.sleep(200);
        }
    }
}