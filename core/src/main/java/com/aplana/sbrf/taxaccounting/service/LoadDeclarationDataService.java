package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TransportFileInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;
import java.util.Map;

/**
 * Сервис загрузки ТФ деклараций
 *
 * @author lhaziev
 */
public interface LoadDeclarationDataService {
    /**
     * Загрузка ТФ ТБ (FORM_UPLOAD_DIRECTORY)
     * Все ТБ
     */
    ImportCounter importDeclaration(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync);

    /**
     * Загрузка ТФ НФ ТБ (FORM_UPLOAD_DIRECTORY)
     * Указанные ТБ и список имен файлов
     */
    ImportCounter importDeclaration(TAUserInfo userInfo, Map<Integer, List<TaxType>> departmentTaxMap, Logger logger, String lock, boolean isAsync);

    //List<TransportFileInfo> getFormDataFiles(TAUserInfo userInfo, Logger logger);

    /**
     * Карта: Доступные ТБ -> доступные виды налогов
     */
   // Map<Integer, List<TaxType>> getTB(TAUserInfo userInfo, Logger logger);
}