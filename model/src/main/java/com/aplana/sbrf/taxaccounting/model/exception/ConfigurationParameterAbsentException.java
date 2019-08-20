package com.aplana.sbrf.taxaccounting.model.exception;

/**
 * Класс-исключение, используется для идентификации ситуаций,
 * когда Конфигурационный Параметр отсутствует или является незаполненным
 */
public class ConfigurationParameterAbsentException extends IllegalStateException {
    public ConfigurationParameterAbsentException(String message) {
        super(message);
    }
}
