package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.File;

/**
 * Сервис загрузки ТФ деклараций
 */
public interface LoadDeclarationDataService {

    /**
     * Импортировать в систему данные из ТФ в формате xml.
     *
     * @param xmlTransportFile импортируемый файл
     * @param xmlFileName      название файла
     * @param declarationData  целевая ПНФ
     * @param userInfo         данные пользователя, загрузившего файл
     * @param logger           логгер панели уведомлений
     */
    void importXmlTransportFile(File xmlTransportFile, String xmlFileName, DeclarationData declarationData, TAUserInfo userInfo, Logger logger);
}
