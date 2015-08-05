package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TransportFileInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;

/**
 * Сервис загрузки ТФ НФ
 *
 * @author Dmitriy Levykin
 */
public interface LoadFormDataService {
    /**
     * Загрузка ТФ НФ ТБ (FORM_UPLOAD_DIRECTORY)
     * Все ТБ
     */
    ImportCounter importFormData(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync);

    /**
     * Загрузка ТФ НФ ТБ (FORM_UPLOAD_DIRECTORY)
     * Указанные ТБ и список имен файлов
     */
    ImportCounter importFormData(TAUserInfo userInfo, List<Integer> departmentIdList, List<String> loadedFileNameList,
                                 Logger logger, String lock, boolean isAsync);

    List<TransportFileInfo> getFormDataFiles(TAUserInfo userInfo, List<Integer> departmentIdList,
                                             List<String> loadedFileNameList, Logger logger);
    /**
     * Список доступных ТБ
     */
    List<Integer> getTB(TAUserInfo userInfo, Logger logger);
}
