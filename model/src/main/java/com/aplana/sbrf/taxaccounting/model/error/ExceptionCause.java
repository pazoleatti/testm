package com.aplana.sbrf.taxaccounting.model.error;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Класс - обертка для отображения одного исключения
 */
public class ExceptionCause {
    ExceptionCause() {
    }
    public ExceptionCause (Set<String> serverException, String message, String errorClass) {
        setServerException(serverException);
        setMessage(message);
        setErrorClass(errorClass);
    }
    /**
     * Стэк исключения набором строк
     */
    private Set<String> serverException = Sets.newHashSet();
    /**
     * текст исключения
     */
    private String message;
    /**
     * класс исключения строкой
     */
    private String errorClass;

    public Set<String> getServerException() {
        return serverException;
    }

    public void setServerException(Set<String> serverException) {
        this.serverException = serverException;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorClass() {
        return errorClass;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }
}