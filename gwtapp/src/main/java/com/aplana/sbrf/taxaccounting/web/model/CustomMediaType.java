package com.aplana.sbrf.taxaccounting.web.model;

import org.springframework.http.MediaType;


/**
 * Класс для работы с кастомными типами контента в спецификации HTTP
 */
public class CustomMediaType {
    /**
     * MediaType.TEXT_HTML_VALUE = "text/html"
     */
    public static final String TEXT_HTML_UTF8_VALUE = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8";
    public static final String APPLICATION_ZIP_VALUE = "application/zip";
    public static final String APPLICATION_VND_UTF8_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8";
}