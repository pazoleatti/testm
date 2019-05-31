package com.aplana.sbrf.taxaccounting.model.json;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * Парсер даты из текстового формата ISO в объекты типа Date
 */
public class ISODateDeserializer extends JsonDeserializer<Date> {
    private final FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");

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
