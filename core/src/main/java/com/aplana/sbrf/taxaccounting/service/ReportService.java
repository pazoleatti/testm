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
	String get(TAUserInfo userInfo, long formDataId, ReportType type, boolean checking, boolean manual, boolean absolute);

    /**
     * Удаление всех отчетов по id НФ
     * @param formDataId
     */
    void delete(long formDataId);
}
