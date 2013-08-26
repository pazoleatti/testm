package com.aplana.sbrf.taxaccounting.service;

/**
 * Сервис для маппинга атрибутов рну сторонних систем на атрибуты новой системы
 *
 * @author Alexande Ivanov
 */
public interface RnuMappingService {

    public void addFormDataFromRnuFile(String filename, byte[] fileContent);

}
