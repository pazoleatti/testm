package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

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
     * Создание записи об отчете декларации
     * @param declarationDataId
     * @param blobDataId
     * @param type
     */
    void createDec(long declarationDataId, String blobDataId, ReportType type);

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
     * Получение записи об отчете декларации
     * @param userInfo
     * @param declarationDataId
     * @param type
     * @return uuid
     */
    String getDec(TAUserInfo userInfo, long declarationDataId, ReportType type);

    /**
     * Удаление всех отчетов по id НФ
     * @param formDataId
     */
    void delete(long formDataId);

    /**
     * Удаление всех отчетов по id декларации
     * @param declarationDataId
     */
    void deleteDec(long declarationDataId);
}
