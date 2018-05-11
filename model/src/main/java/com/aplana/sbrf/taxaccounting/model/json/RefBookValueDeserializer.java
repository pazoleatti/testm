package com.aplana.sbrf.taxaccounting.model.json;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Десериализатор для enum RefBookValue в виде объекта
 */
public class RefBookValueDeserializer extends JsonDeserializer<RefBookValue> {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public RefBookValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = jp.readValueAsTree();
        RefBookAttributeType attributeType = RefBookAttributeType.valueOf(jsonNode.get("attributeType").asText());
        Object value = null;
        switch (attributeType) {
            case STRING:
                value = jsonNode.get("stringValue").asText();
                break;
            case NUMBER:
                value = NumberUtils.createNumber(jsonNode.get("numberValue").asText());
                break;
            case DATE:
                try {
                    value = !jsonNode.get("dateValue").isNull() ? format.parse(jsonNode.get("dateValue").asText()) : null;
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Не удалось получить дату из строки: " + jsonNode.get("dateValue").asText());
                }
                break;
            case REFERENCE:
                value = jsonNode.get("referenceValue").asLong();
                break;
            default:
                throw new IllegalArgumentException("Unknown attribute type: " + attributeType);
        }
        return new RefBookValue(attributeType, value);
    }
}
