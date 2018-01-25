package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;

import java.util.Collection;
import java.util.List;

/**
 * DAO-Интерфейс для работы с таблицей отчетов
 */
public interface ReportDao {
    /**
     * Создание записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param blobDataId        идентификатор блоба
     * @param type              тип отчета
     */
    void createDec(long declarationDataId, String blobDataId, DeclarationDataReportType type);

    /**
     * Получение записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     * @return uuid идентификатор блоба
     */
    String getDec(long declarationDataId, DeclarationDataReportType type);

    /**
     * Удаление всех отчетов по id декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    void deleteDec(long declarationDataId);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataIds идентификаторы деклараций
     */
    void deleteDec(Collection<Long> declarationDataIds);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     */
    void deleteDec(long declarationDataId, DeclarationDataReportType type);

    /**
     * Удаляет отчеты деклараций по типам
     *
     * @param declarationDataIds идентификаторы декларации
     * @param ddReportTypes      типы отчета
     */
    void deleteDec(Collection<Long> declarationDataIds, List<DeclarationDataReportType> ddReportTypes);

    /**
     * Удаление отчета по uuid
     *
     * @param uuid идентификатор блоба
     */
    void deleteDec(String uuid);

    /**
     * Удаление ненужных/устаревших отчетов
     *
     * @return Количество удаленных запсией
     */
    int clean();
}
