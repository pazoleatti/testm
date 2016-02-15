package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.ReportType;

import java.util.Collection;
import java.util.List;

/**
 * DAO-Интерфейс для работы с таблицей отчетов
 */
public interface ReportDao {
    /**
     * Создание записи об отчете НФ
     * @param formDataId
     * @param blobDataId
     * @param type
     * @param checking
     * @param manual
     * @param absolute
     */
    void create(long formDataId, String blobDataId, String type, boolean checking, boolean manual, boolean absolute);

    /**
     * Создание записи об отчете декларации
     * @param declarationDataId
     * @param blobDataId
     * @param type
     */
    void createDec(long declarationDataId, String blobDataId, String type);

    /**
     * Создание записи об отчете декларации
     * @param declarationDataId
     * @param blobDataId
     * @param type
     */
    void createAudit(Integer userId, String blobDataId, ReportType type);

    /**
     * Получение записи об отчете НФ
     * @param formDataId
     * @param type
     * @param checking
     * @param manual
     * @param absolute
     * @return uuid
     */
    String get(long formDataId, String type, boolean checking, boolean manual, boolean absolute);

    /**
     * Получение записи об отчете декларации
     * @param declarationDataId
     * @param type
     * @return uuid
     */
    String getDec(long declarationDataId, String type);

    /**
     * Получение записи об отчете журнала аудита
     * @param userInfo
     * @param declarationDataId
     * @param type
     * @return uuid
     */
    String getAudit(Integer userId, ReportType type);

    /**
     * Удаление всех отчетов для НФ
     * @param formDataId
     * @param manual
     */
    void delete(long formDataId, Boolean manual);

    /**
     * Удаление всех отчетов по id декларации
     * @param declarationDataId
     */
    void deleteDec(long declarationDataId);

    void deleteDec(Collection<Long> declarationDataIds);

    void deleteDec(Collection<Long> declarationDataIds, List<String> reportTypes);

    /**
     * Удаление отчета по uuid
     * @param uuid
     */
    void deleteDec(String uuid);

    void deleteAudit(int userId, ReportType reportType);

    void deleteAudit(String blobDataId);

    /**
     * Удаление ненужных/устаревших отчетов
     * @return Количество удаленных запсией
     */
    int clean();
}
