package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Map;

/**
 * Интерфейс сервиса, реализующего выполение скриптов над данными налоговых форм.
 */
public interface FormDataScriptingService {

	/**
	 * Выполняет скрипт формы (FormTemplate.script) по определенному событию.
	 *
	 * @param userInfo информация о текущем пользоваетеле
	 * @param formData данные налоговой формы
	 * @param event    событие
	 * @param logger   логгер для сохранения ошибок выполнения скриптов
	 * @param additionalParameters дополнительные параметры для передачи в скрипты. Их состав зависит от события для которого вызываются
	 *                             скрипты. Параметр может иметь значение null
	 * @return true - скрипт был исполнен, false - скрипт не запускался
	 */
	boolean executeScript(TAUserInfo userInfo, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters);

    /**
     * Выполняет скрипт формы (FormTemplate.script) по определенному событию(без сохранения изменений в БД).
     *
     * @param userInfo информация о текущем пользоваетеле
     * @param script скрипт макета НФ
     * @param formData данные налоговой формы
     * @param event    событие
     * @param logger   логгер для сохранения ошибок выполнения скриптов
     * @param additionalParameters дополнительные параметры для передачи в скрипты. Их состав зависит от события для которого вызываются
     *                             скрипты. Параметр может иметь значение null
     * @return true - скрипт был исполнен, false - скрипт не запускался
     */
    boolean executeScriptInNewReadOnlyTransaction(TAUserInfo userInfo, String script, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters);
}