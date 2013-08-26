package com.aplana.sbrf.taxaccounting.service;

/**
 * Сервис для маппинга атрибутов рну сторонних систем на атрибуты новой системы
 * Используется для РНУ с ТФ с расширением xml
 * @author Alexande Ivanov
 */
public interface XmlMappingService {

    public void addFormDataFrom(String filename, byte[] fileContent);

}
