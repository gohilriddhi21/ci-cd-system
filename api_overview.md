
# 🌐 API Control Component Documentation

### Component Type: **Orchestration + Service Integration**

This module connects a command-line interface (CLI) with the backend server using **Server-Sent Events (SSE)** to support long-running operations with real-time output.

---

## 📡 API Overview

### 1. `POST /api/initial`
Used to send an initial message from the client to the server. Was used during testing connections.  
Can also be repurposed for heartbeat connections.

- **Input**: Raw string payload 
- **Output**: 
     - `200 OK` with a confirmation message OR 
     - `400 Bad Request` if null
- **Error**:
  - `400 Bad Request:` When the request body is missing

---

### 2. `GET /execute`
Establishes an **SSE stream** and sends real-time events to the client while executing the CLI command.

- **Input**: 
  - `command` (string query parameter): The raw command to be parsed and executed.

- **Output**: 
  - Streaming response (`text/event-stream`) with `data:`-prefixed messages

- **Errors / Failures**:
  - `Error: No command provided.` — When `command` is null or empty
  - `Error: <message>` — If CLI execution throws an exception
  - If emitter fails, the server completes the SSE with an error

- **Success Flow**:
  - CLI command is split and passed to `CommandLineHandler`
  - Server responds with live `data:` events via `SseEmitter`
  - The final message may be sent using `sendEventAndComplete`

---

## 🔄 Server-Sent Events (SSE) Protocol

- **Purpose**: Enables continuous one-way communication from server to client. Ideal for real-time CLI execution logs, progress updates, or job results.

- **Key Classes**:
  - `SseEmitter` — Spring's implementation for streaming
  - `CLI` — Client class that parses and listens to SSE using Java HTTP client

- **Flow**:
  1. `CLI.run()` constructs `GET /execute?command=...`
  2. Backend processes command in a separate thread
  3. Each output is streamed back via `emitter.send()`
  4. Upon completion or failure, `emitter.complete()` or `emitter.completeWithError()` is called. 

- **Client Output Options**:
  - `--format JSON`: Outputs raw JSON
  - `--format table`: Outputs tabular data using `ReportProcessor`
  - Can be filtered by `--format table` or `--format JSON` (Default is JSON)

---

## 🧭 Public API Documentation for CLI & Server Units

This section outlines the **public APIs** and **public methods** in the CLI and SSE server components. Each method's responsibility is clearly aligned with the function of the component it belongs to.

---

## 🧩 Component: CLI (Client)
**Package:** `com.example.cliapplication.service`

### ➕ Public Constructors
- `CLI(String[] args)`
  - Default constructor, auto-injects dependencies.
- `CLI(String[] args, CommandLineHandler, ConsoleOutput, HttpClient)`
  - Custom constructor for dependency injection.

### 🚀 Public Method
- `void run()`
  - **Responsibility**: Entry point to parse CLI arguments, build the SSE request, and handle responses.
  - Delegates parsing, validation, streaming, and formatting logic to helpers.

---

## 🔁 Server-Sent Events (SSE) Interactions

### 📡 URI: `GET /execute`
- **Triggered by**: `CLI.run()`
- **Built using**: `createSseUri(String command)`

### 📤 SSE Request Handling
- `sendSseRequest(HttpRequest, Consumer<HttpResponse<Stream<String>>>)`
  - Sends the request to `/execute` and streams response lines to handler.

### 📥 SSE Response Processing
- `processSseResponse(HttpResponse<Stream<String>>, CommandLine)`
  - Parses `data:` lines and forwards output to console or `ReportProcessor`.

---

## 📊 Output Management
- `processTableOutput(String jsonData, CommandLine cmd)`
  - Converts raw JSON string into formatted output (table or JSON).
- `hasRawArg(String argName)`
  - Utility to detect if a raw CLI flag is present in `args`.

---

## 🧩 Component: Server Controller
**Class:** `SseController`

### 📡 API: `POST /api/initial`
- **Method**: `ResponseEntity<String> receiveInitialMessage(String message)`
  - Accepts a basic payload for initialization or logging purposes.

### 📡 API: `GET /execute`
- **Method**: `SseEmitter receiveRawCommand(String command)`
  - Accepts a raw shell-style command and uses SSE to emit streaming updates during command execution.

### 🧵 Internal Event Helpers
- `void sendEvent(SseEmitter emitter, String data)`
  - Sends a streaming event to the client.
- `void sendEventAndComplete(SseEmitter emitter, String data)`
  - Sends the final event and completes the stream.
- `static void sendEvent(String data)`
  - Allows static dispatching from other components (like `CommandLineHandler`).

---

## 📌 Notes on Alignment

- Each **public method** is directly related to a **single responsibility**: parsing commands, sending requests, handling responses, or managing SSE.
- **Server methods** are REST/SSE entry points or emitter utility methods.
- **Client methods** separate parsing, streaming, and formatting into discrete, testable units.
