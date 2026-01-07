package br.com.tc4.notify_critical_feedback.deserializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Deserializer {
    private final ObjectMapper objectMapper;

    public Deserializer() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        
        this.objectMapper = objectMapper;
    }

    public <T> T deserialize(String json, Class<T> valueType) throws Exception {
        return objectMapper.readValue(json, valueType);
    }
}
