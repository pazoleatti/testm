package com.aplana.sbrf.taxaccounting.model.json;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

public class RefBookValueSerializer extends JsonSerializer<RefBookValue> {
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void serialize(RefBookValue value, JsonGenerator json, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        serializeValue(value, json);
    }

    private void serializeValue(RefBookValue value, JsonGenerator json) throws IOException {
        json.writeStartObject();
        json.writeStringField("attributeType", value.getAttributeType().name());
        if (value.getValue() == null) {
            json.writeNullField("value");
        } else {
            switch (value.getAttributeType()) {
                case STRING:
                    json.writeStringField("value", value.getStringValue());
                    break;
                case NUMBER:
                    //TODO: сейчас в наших атрибутах не хранятся числа с плавающей точкой, поэтому так
                    json.writeNumberField("value", value.getNumberValue().longValue());
                    break;
                case DATE:
                    json.writeStringField("value", DF.format(value.getDateValue()));
                    break;
                case REFERENCE:
                    json.writeNumberField("value", value.getReferenceValue());
                    break;
                case COLLECTION:
                    json.writeFieldName("collectionValue");
                    json.writeStartArray();
                    for (Map<String, RefBookValue> record : value.getCollectionValue()) {
                        json.writeStartObject();
                        for (Map.Entry<String, RefBookValue> child : record.entrySet()) {
                            json.writeFieldName(child.getKey());
                            serializeValue(child.getValue(), json);
                        }
                        json.writeEndObject();
                    }
                    json.writeEndArray();
                    break;
            }
        }
        json.writeEndObject();
    }
}
