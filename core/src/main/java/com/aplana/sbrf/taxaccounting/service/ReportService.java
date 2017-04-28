package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс для работы с таблицей отчетов
 * @author lhaziev
 *
 */
public interface ReportService {
    /**
     * Создание записи об отчете декларации
     * @param declarationDataId
     * @param blobDataId
     * @param type
     */
    void createDec(long declarationDataId, String blobDataId, DeclarationDataReportType type);

    /**
     * Создание записи об отчете журнала аудита
     * @param userId ссылка на пользователя
     * @param blobDataId ссылка на отчет
     * @param type тип
     */
    void createAudit(Integer userId, String blobDataId, ReportType type);

    /**
     * Получение записи об отчете декларации
     * @param userInfo
     * @param declarationDataId
     * @param type
     * @return uuid
     */
    String getDec(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType type);

    /**
     * Получение записи об отчете журнала аудита
     * @param userInfo
     * @param type
     * @return uuid
     */
    String getAudit(TAUserInfo userInfo, ReportType type);

    /**
     * Удаление всех отчетов по id декларации
     * @param declarationDataId
     */
    void deleteDec(long declarationDataId);

    void deleteDec(Collection<Long> declarationDataId);

    void deleteDec(Collection<Long> declarationDataId, List<DeclarationDataReportType> reportTypes);

    /**
     * Удаление отчета по uuid
     * @param uuid
     */
    void deleteDec(String uuid);

    /**
     * @param declarationDataId
     */
    void deleteAudit(TAUserInfo userInfo, ReportType reportType);

    /**
     * Удаление всех отчетов по id blobdata
     * @param declarationDataId
     */
    void deleteAudit(String blobDataId);
}
