package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
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
    ImportCounter importFormData(TAUserInfo userInfo, Logger logger);

    /**
     * Загрузка ТФ НФ ТБ (FORM_UPLOAD_DIRECTORY)
     * Указанные ТБ
     */
    ImportCounter importFormData(TAUserInfo userInfo, List<Integer> departmentIdList,  Logger logger);
}
