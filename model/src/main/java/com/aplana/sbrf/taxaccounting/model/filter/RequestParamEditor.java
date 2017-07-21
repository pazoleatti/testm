package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.PropertyEditorSupport;

/**
 * Класс для разбора пришедшего в запросе элемента фильтра
 */
public class RequestParamEditor extends PropertyEditorSupport{
    private Class targetClass;

    public RequestParamEditor(Class targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        ObjectMapper objectMapper = new ObjectMapper();
        Logger logger = new Logger();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonFactory jsonFactory = objectMapper.getFactory();

        try {
            JsonParser jsonParser = jsonFactory.createParser(text);
            setValue(objectMapper.readValue(jsonParser, targetClass));
        } catch (Exception e) {
            logger.error(String.format("Can not parse %s from value: %s", targetClass.getName(), text), e);
        }
    }

    @Override
    public String getAsText() {
        Logger logger = new Logger();
        try {
            return new ObjectMapper().writeValueAsString(getValue());
        } catch (JsonProcessingException e) {
            logger.error("Can not convert param to json");
            return "";
        }
    }
}
