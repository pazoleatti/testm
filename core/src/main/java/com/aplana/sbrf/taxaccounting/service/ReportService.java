package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.FormDataReportType;
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
    void create(long formDataId, String blobDataId, FormDataReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Создание записи об отчете декларации
     * @param declarationDataId
     * @param blobDataId
     * @param type
     */
    void createDec(long declarationDataId, String blobDataId, DeclarationDataReportType type);

    /**
     * Получение записи об отчете НФ
     * @param formDataId
     * @param type
     * @param checking
     * @param manual
     * @param absolute
     * @return uuid
     */
	String get(TAUserInfo userInfo, long formDataId, FormDataReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Получение записи об отчете декларации
     * @param userInfo
     * @param declarationDataId
     * @param type
     * @return uuid
     */
    String getDec(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType type);

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

    void deleteDec(Collection<Long> declarationDataId, List<DeclarationDataReportType> reportTypes);

    /**
     * Удаление отчета по uuid
     * @param uuid
     */
    void deleteDec(String uuid);
}
