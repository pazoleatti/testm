package com.aplana.sbrf.taxaccounting.model.json;

import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Десериализатор для enum DeclarationFormKind в виде объекта
 * можно через @JsonCreator, но с ним в GWT ошибка
 */
public class JsonDeclarationFormKindDeserializer extends JsonDeserializer<DeclarationFormKind> {
    @Override
    public DeclarationFormKind deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = jp.readValueAsTree();
        return DeclarationFormKind.fromId(jsonNode.get("id").asLong());
    }
}
