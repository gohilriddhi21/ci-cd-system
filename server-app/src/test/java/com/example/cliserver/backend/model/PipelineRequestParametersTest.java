package com.example.cliserver.backend.model;

import com.example.cliserver.backend.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineRequestParametersTest {

    private PipelineRequestParameters params;

    @BeforeEach
    void setUp() {
        params = new PipelineRequestParameters();
    }

    @Test
    void testEmptyConstructor() {
        assertTrue(params.getAll().isEmpty(), "New instance should have empty parameters map");
    }

    @Test
    void testAddAndGet() {
        params.add("key1", "value1");
        assertEquals("value1", params.get("key1"), "Should return the correct value for an existing key");
        assertNull(params.get("nonexistentKey"), "Should return null for a non-existent key");
    }

    @Test
    void testAddReturnsThis() {
        PipelineRequestParameters result = params.add("key1", "value1");
        assertSame(params, result, "Add method should return the same instance for method chaining");
    }

    @Test
    void testAddMultipleTypes() {
        params.add("stringKey", "stringValue")
            .add("intKey", 42)
            .add("booleanKey", true)
            .add("objectKey", new Object());

        assertEquals("stringValue", params.get("stringKey"));
        assertEquals(42, params.get("intKey"));
        assertEquals(true, params.get("booleanKey"));
        assertNotNull(params.get("objectKey"));
    }

    @Test
    void testGetWithDefault() {
        params.add("existingKey", "existingValue");

        assertEquals("existingValue", params.get("existingKey", "defaultValue"),
            "Should return the actual value when key exists");
        assertEquals("defaultValue", params.get("nonexistentKey", "defaultValue"),
            "Should return the default value when key doesn't exist");
    }

    @Test
    void testHas() {
        params.add("key1", "value1");

        assertTrue(params.has("key1"), "Should return true for existing key");
        assertFalse(params.has("nonexistentKey"), "Should return false for non-existent key");
    }

    @Test
    void testGetAll() {
        params.add("key1", "value1")
            .add("key2", "value2");

        Map<String, Object> allParams = params.getAll();
        assertEquals(2, allParams.size(), "Should return all parameters");
        assertEquals("value1", allParams.get("key1"));
        assertEquals("value2", allParams.get("key2"));
        allParams.put("key3", "value3");
        assertFalse(params.has("key3"), "Modifying the returned map should not affect the original");
    }

    @Test
    void testConvenienceMethods() {
        params.add("filename", "test.txt")
            .add("repo", "testRepo")
            .add("branch", "main")
            .add("commit", "abc123")
            .add("verboseLogging", true)
            .add("pipelineName", "buildPipeline")
            .add("stage", "buildStage")
            .add("job", "compileJob")
            .add("runNumber", "42")
            .add("format", "json")
            .add("isLocal", true)
            .add("localDir", "/custom/path");

        assertEquals("test.txt", params.getFilename());
        assertEquals("testRepo", params.getRepo());
        assertEquals("main", params.getBranch());
        assertEquals("abc123", params.getCommit());
        assertTrue(params.getVerboseLogging());
        assertEquals("buildPipeline", params.getPipelineName());
        assertEquals("buildStage", params.getStage());
        assertEquals("compileJob", params.getJob());
        assertEquals("42", params.getRunNumber());
        assertEquals("json", params.getFormat());
        assertTrue(params.isLocal());
        assertEquals("/custom/path", params.getLocalDirPath());
    }

    @Test
    void testDefaultValues() {
        assertFalse(params.getVerboseLogging(), "verboseLogging should default to false");
        assertFalse(params.isLocal(), "isLocal should default to false");
        assertEquals(Constants.DIRECTORY, params.getLocalDirPath(),
            "localDir should default to Constants.DIRECTORY");
    }

    @Test
    void testNullHandling() {
        params.add("nullKey", null);

        assertNull(params.get("nullKey"), "Should handle null values properly");
        assertTrue(params.has("nullKey"), "has() should return true even for null values");
    }

    @Test
    void testParameterOverwrite() {
        params.add("key", "originalValue");
        assertEquals("originalValue", params.get("key"));

        params.add("key", "newValue");
        assertEquals("newValue", params.get("key"), "Adding with same key should overwrite the previous value");
    }

    @Test
    void testTypeCasting() {
        params.add("intValue", 42);
        Exception exception = assertThrows(ClassCastException.class, () -> {
            String value = params.getFilename();
            String intAsString = (String) params.get("intValue");
        });
        params.add("filename", "test.txt");
        params.add("verboseLogging", true);

        String filename = params.getFilename();
        Boolean verboseLogging = params.getVerboseLogging();

        assertEquals("test.txt", filename);
        assertTrue(verboseLogging);
    }

    @Test
    void testConvenienceMethodsWithMissingParameters() {
        assertNull(params.getFilename());
        assertNull(params.getRepo());
        assertNull(params.getBranch());
        assertNull(params.getCommit());
        assertNull(params.getPipelineName());
        assertNull(params.getStage());
        assertNull(params.getJob());
        assertNull(params.getRunNumber());
        assertNull(params.getFormat());
    }
}