package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;
import java.util.Set;

/**
 * Сервис загрузки транспортных файлов
 *
 * @author Dmitriy Levykin
 */
public interface LoadTransportDataService {

    /**
     * Загрузка ТФ справочников из ЦАС НСИ (ACCOUNT_PLAN_UPLOAD_DIRECTORY, OKATO_UPLOAD_DIRECTORY, REGION_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookNsi(TAUserInfo userInfo, Logger logger);

    /**
     * Загрузка ТФ справочников из Diasoft (DIASOFT_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookDiasoft(TAUserInfo userInfo, Logger logger);

    /**
     * Загрузка ТФ НФ ТБ (FORM_UPLOAD_DIRECTORY)
     */
    ImportCounter importFormData(TAUserInfo userInfo, Logger logger);

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    List<String> getWorkTransportFiles(String folderPath, Set<String> ignoreFileSet);
}
