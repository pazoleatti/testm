package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.ReportType;

import java.util.Collection;
import java.util.List;

/**
 * DAO-Интерфейс для работы с таблицей отчетов
 */
public interface ReportDao {
    /**
     * Создание записи об отчете декларации
     * @param declarationDataId
     * @param blobDataId
     * @param type
     */
    void createDec(long declarationDataId, String blobDataId, DeclarationDataReportType type);

    /**
     * Получение записи об отчете декларации
     * @param declarationDataId
     * @param type
     * @return uuid
     */
    String getDec(long declarationDataId, DeclarationDataReportType type);

    /**
     * Удаление всех отчетов по id декларации
     * @param declarationDataId
     */
    void deleteDec(long declarationDataId);

    void deleteDec(Collection<Long> declarationDataIds);

    void deleteDec(Collection<Long> declarationDataIds, List<DeclarationDataReportType> ddReportTypes);

    /**
     * Удаление отчета по uuid
     * @param uuid
     */
    void deleteDec(String uuid);

    /**
     * Удаление ненужных/устаревших отчетов
     * @return Количество удаленных запсией
     */
    int clean();
}
