package com.aplana.sbrf.taxaccounting.model.json;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Парсер даты из текстового формата ISO в объекты типа Date
 *
 * @author dloshkarev
 */
public class ISODateDeserializer extends JsonDeserializer<Date> {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new Date(jp.getLongValue());
        }
        if (t == JsonToken.VALUE_STRING) {
            try {
                return format.parse(jp.getText().trim());
            } catch (ParseException e) {
                throw new ServiceException("Не удалось получить дату из строки: " + jp.getText().trim());
            }
        }
        throw ctxt.mappingException(handledType());
    }
}
