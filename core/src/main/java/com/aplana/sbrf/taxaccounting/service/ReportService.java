package com.aplana.sbrf.taxaccounting.service;

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
     * Создание записи об отчете НФ
     * @param formDataId
     * @param blobDataId
     * @param type
     * @param checking
     * @param manual
     * @param absolute
     */
    void create(long formDataId, String blobDataId, ReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Создание записи о специфичном отчете НФ
     * @param formDataId
     * @param blobDataId
     * @param specificReportType
     * @param checking
     * @param manual
     * @param absolute
     */
    void create(long formDataId, String blobDataId, String specificReportType, boolean checking, boolean manual, boolean absolute);

        /**
         * Создание записи об отчете декларации
         * @param declarationDataId
         * @param blobDataId
         * @param type
         */
    void createDec(long declarationDataId, String blobDataId, ReportType type);

    /**
     * Создание записи об отчете журнала аудита
     * @param userId ссылка на пользователя
     * @param blobDataId ссылка на отчет
     * @param type тип
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
	String get(TAUserInfo userInfo, long formDataId, ReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Получение записи о специфичном отчете НФ
     * @param userInfo
     * @param formDataId
     * @param specificReportType
     * @param checking
     * @param manual
     * @param absolute
     * @return
     */
    String get(TAUserInfo userInfo, long formDataId, String specificReportType, boolean checking, boolean manual, boolean absolute);

        /**
         * Получение записи об отчете декларации
         * @param userInfo
         * @param declarationDataId
         * @param type
         * @return uuid
         */
    String getDec(TAUserInfo userInfo, long declarationDataId, ReportType type);

    /**
     * Получение записи об отчете журнала аудита
     * @param userInfo
     * @param type
     * @return uuid
     */
    String getAudit(TAUserInfo userInfo, ReportType type);

    /**
     * Удаление всех отчетов по id НФ
     * @param formDataId
     * @param manual
     */
    void delete(long formDataId, Boolean manual);

    /**
     * Удаление всех отчетов по id декларации
     * @param declarationDataId
     */
    void deleteDec(long declarationDataId);

    void deleteDec(Collection<Long> declarationDataId);

    void deleteDec(Collection<Long> declarationDataId, List<ReportType> reportTypes);

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
