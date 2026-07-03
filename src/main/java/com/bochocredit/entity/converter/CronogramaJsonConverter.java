package com.bochocredit.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Convierte el cronograma (List&lt;Map&lt;String,Object&gt;&gt;) hacia/desde
 * una columna JSONB de PostgreSQL. Equivalente a json.dumps()/json.loads()
 * en el backend Flask original.
 */
@Converter
public class CronogramaJsonConverter implements AttributeConverter<List<Map<String, Object>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Error serializando cronograma a JSON", e);
        }
    }

    @Override
    public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Error deserializando cronograma desde JSON", e);
        }
    }
}
