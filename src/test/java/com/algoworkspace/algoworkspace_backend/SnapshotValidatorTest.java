package com.algoworkspace.algoworkspace_backend;

import com.algoworkspace.algoworkspace_backend.validator.SnapshotValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotValidatorTest {

    private SnapshotValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SnapshotValidator();
    }

    @Test
    void testValidSnapshot() {
        String validSnapshot = """
                {
                    "components": [
                        {
                            "id": "array-1",
                            "type": "ARRAY",
                            "values": [2, 7, 11, 15]
                        },
                        {
                            "id": "variable-1",
                            "type": "VARIABLE",
                            "name": "i",
                            "value": 0
                        }
                    ]
                }
                """;

        assertDoesNotThrow(() -> validator.validate(validSnapshot));
    }

    @Test
    void testEmptySnapshot() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("")
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testNullSnapshot() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(null)
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testMissingComponents() {
        String invalidSnapshot = "{\"step\": 1}";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("components"));
    }

    @Test
    void testComponentsNotArray() {
        String invalidSnapshot = "{\"components\": \"not-an-array\"}";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("must be an array"));
    }

    @Test
    void testComponentMissingId() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "type": "ARRAY",
                            "values": [1, 2, 3]
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    void testComponentMissingType() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "array-1",
                            "values": [1, 2, 3]
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("type"));
    }

    @Test
    void testUnknownComponentType() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "unknown-1",
                            "type": "UNKNOWN_TYPE",
                            "data": "something"
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("unknown type"));
    }

    @Test
    void testArrayComponentMissingValues() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "array-1",
                            "type": "ARRAY"
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("values"));
    }

    @Test
    void testArrayComponentValuesNotArray() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "array-1",
                            "type": "ARRAY",
                            "values": "not-an-array"
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("must be an array"));
    }

    @Test
    void testArrayComponentNonNumericValue() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "array-1",
                            "type": "ARRAY",
                            "values": [1, "two", 3]
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("must be a number"));
    }

    @Test
    void testVariableComponentMissingName() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "var-1",
                            "type": "VARIABLE",
                            "value": 5
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    void testVariableComponentMissingValue() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "var-1",
                            "type": "VARIABLE",
                            "name": "count"
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("value"));
    }

    @Test
    void testVariableComponentNonNumericValue() {
        String invalidSnapshot = """
                {
                    "components": [
                        {
                            "id": "var-1",
                            "type": "VARIABLE",
                            "name": "count",
                            "value": "not-a-number"
                        }
                    ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(invalidSnapshot)
        );
        assertTrue(exception.getMessage().contains("must be a number"));
    }

    @Test
    void testMultipleComponents() {
        String validSnapshot = """
                {
                    "components": [
                        {
                            "id": "array-1",
                            "type": "ARRAY",
                            "values": [1, 2, 3]
                        },
                        {
                            "id": "array-2",
                            "type": "ARRAY",
                            "values": [4, 5, 6]
                        },
                        {
                            "id": "var-1",
                            "type": "VARIABLE",
                            "name": "left",
                            "value": 0
                        },
                        {
                            "id": "var-2",
                            "type": "VARIABLE",
                            "name": "right",
                            "value": 5
                        }
                    ]
                }
                """;

        assertDoesNotThrow(() -> validator.validate(validSnapshot));
    }

    @Test
    void testEmptyComponentsArray() {
        String validSnapshot = "{\"components\": []}";
        assertDoesNotThrow(() -> validator.validate(validSnapshot));
    }
}
