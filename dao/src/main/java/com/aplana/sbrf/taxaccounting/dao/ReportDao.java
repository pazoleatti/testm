package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.ReportType;

/**
 * DAO-Интерфейс для работы с таблицей отчетов
 */
public interface ReportDao {
    /**
     * Создание записи
     * @param formDataId
     * @param blobDataId
     * @param type
     * @param checking
     * @param manual
     * @param absolute
     */
    void create(long formDataId, String blobDataId, ReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Получение записи
     * @param formDataId
     * @param type
     * @param checking
     * @param manual
     * @param absolute
     * @return uuid
     */
    String get(long formDataId, ReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Удаление всех отчетов для НФ
     * @param formDataId
     */
    void delete(long formDataId);
}
