package com.aplana.sbrf.taxaccounting.service;

/**
 * Сервис для маппинга атрибутов рну сторонних систем на атрибуты новой системы
 *
 * Используется только при миграции
 *
 * @author Alexander Ivanov
 */
public interface MappingService {
    /**
     * Создает налоговую форму и импортирует данные из входящего траспортного файла
     *
     * @param filename    название файла с расширением xml или rnu
     * @param fileContent содержимое файла
     */
    public void addFormData(String filename, byte[] fileContent);
}
