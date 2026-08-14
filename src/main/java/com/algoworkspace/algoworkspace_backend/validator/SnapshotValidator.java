package com.algoworkspace.algoworkspace_backend.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SnapshotValidator {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Validates that the snapshot JSON matches the expected structure:
     * - Must be valid JSON
     * - Must have a "components" array
     * - Each component must have "id" and "type"
     * - ARRAY components must have "values" array
     * - VARIABLE components must have "name" and "value"
     */
    public void validate(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Snapshot JSON cannot be null or empty");
        }
        
        try {
            JsonNode root = objectMapper.readTree(snapshotJson);
            
            // Check for components array
            if (!root.has("components")) {
                throw new IllegalArgumentException("Snapshot JSON must contain 'components' array");
            }
            
            JsonNode components = root.get("components");
            if (!components.isArray()) {
                throw new IllegalArgumentException("'components' must be an array");
            }
            
            // Validate each component
            for (int i = 0; i < components.size(); i++) {
                validateComponent(components.get(i), i);
            }
            
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("Invalid snapshot JSON format: " + e.getMessage(), e);
        }
    }
    
    private void validateComponent(JsonNode component, int index) {
        // Check required fields: id and type
        if (!component.has("id") || component.get("id").asText().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Component at index %d is missing required field 'id'", index)
            );
        }
        
        if (!component.has("type") || component.get("type").asText().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Component at index %d is missing required field 'type'", index)
            );
        }
        
        String type = component.get("type").asText();
        
        // Validate based on component type
        switch (type) {
            case "ARRAY":
                validateArrayComponent(component, index);
                break;
            case "VARIABLE":
                validateVariableComponent(component, index);
                break;
            default:
                throw new IllegalArgumentException(
                    String.format("Component at index %d has unknown type '%s'. Valid types: ARRAY, VARIABLE", 
                        index, type)
                );
        }
    }
    
    private void validateArrayComponent(JsonNode component, int index) {
        if (!component.has("values")) {
            throw new IllegalArgumentException(
                String.format("ARRAY component at index %d is missing required field 'values'", index)
            );
        }
        
        JsonNode values = component.get("values");
        if (!values.isArray()) {
            throw new IllegalArgumentException(
                String.format("ARRAY component at index %d: 'values' must be an array", index)
            );
        }
        
        // Validate that all values are numbers
        for (int i = 0; i < values.size(); i++) {
            if (!values.get(i).isNumber()) {
                throw new IllegalArgumentException(
                    String.format("ARRAY component at index %d: value at position %d must be a number", index, i)
                );
            }
        }
    }
    
    private void validateVariableComponent(JsonNode component, int index) {
        if (!component.has("name") || component.get("name").asText().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("VARIABLE component at index %d is missing required field 'name'", index)
            );
        }
        
        if (!component.has("value")) {
            throw new IllegalArgumentException(
                String.format("VARIABLE component at index %d is missing required field 'value'", index)
            );
        }
        
        JsonNode value = component.get("value");
        if (!value.isNumber()) {
            throw new IllegalArgumentException(
                String.format("VARIABLE component at index %d: 'value' must be a number", index)
            );
        }
    }
}
