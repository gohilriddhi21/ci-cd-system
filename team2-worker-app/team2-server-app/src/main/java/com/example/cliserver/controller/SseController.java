package com.example.cliserver.controller;

import com.example.cliserver.cli.CommandLineHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class SseController {
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();
    private static SseEmitter currentEmitter;

    @PostMapping("/api/initial")
    public ResponseEntity<String> receiveInitialMessage(@RequestBody String message) {
        if (message != null) {
            System.out.println("Received initial message: " + message);
            return ResponseEntity.ok("Initial message received: " + message);
        } else {
            return ResponseEntity.badRequest()
                .body("Error: Please provide a 'message' in the payload.");
        }
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "only the initializing thread accesses this field")
    @GetMapping(value = "/execute", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter receiveRawCommand(
            @RequestParam(value = "command", required = false) String command) {
        SseEmitter emitter = new SseEmitter();
        currentEmitter = emitter;

        sseExecutor.execute(() -> {
            try {
                if (command != null && !command.trim().isEmpty()) {
                    String[] args = command.split(" ");
                    try {
                        CommandLineHandler handler = new CommandLineHandler(args, emitter);
                        handler.run();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        sendEvent(emitter,"Error: " + e.getMessage());
                        emitter.complete();
                    }
                } else {
                    sendEvent(emitter,"Error: No command provided.");
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String data) throws IOException {
        emitter.send(SseEmitter.event()
                .data(data));
    }

    public static void sendEvent(String data) throws IOException {
        currentEmitter.send(SseEmitter.event()
                .data(data));
    }

    public static void sendEventAndComplete(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        } finally {
            emitter.complete();
        }
    }
}
