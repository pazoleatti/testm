package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Map;

/**
 * Интерфейс сервиса для запуска скриптов по декларациями
 * @author dsultanbekov
 */
public interface DeclarationDataScriptingService {
	/**
	 *
	 * @param userInfo информация о текущем пользоваетеле
	 * @param declarationData данные декларации
	 * @param event событие
	 * @param logger логгер для сохранения ошибок выполнения скриптов
	 * @param exchangeParams параметры для обратной связи ядра со скриптом
	 * @return true - скрипт был исполнен, false - скрипт не запускался
	 */
	boolean executeScript(TAUserInfo userInfo, DeclarationData declarationData, FormDataEvent event, Logger logger,
                       Map<String, Object> exchangeParams);

    /**
     * Выполняет скрипт декларации (FormTemplate.script) по определенному событию(без сохранения изменений в БД).
     * @param userInfo информация о текущем пользоваетеле
     * @param declarationTemplate макет декларации
     * @param declarationData данные декларации
     * @param event событие
     * @param logger логгер для сохранения ошибок выполнения скриптов
     * @param exchangeParams параметры для обратной связи ядра со скриптом
     * @return true - скрипт был исполнен, false - скрипт не запускался
     */
    boolean executeScriptInNewReadOnlyTransaction(TAUserInfo userInfo, DeclarationTemplate declarationTemplate, DeclarationData declarationData, FormDataEvent event, Logger logger,
                          Map<String, Object> exchangeParams);
}