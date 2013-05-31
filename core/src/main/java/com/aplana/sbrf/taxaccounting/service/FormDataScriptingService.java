package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUser;

import java.util.Map;

/**
 * Интерфейс сервиса, реализующего выполение скриптов над данными налоговых форм.
 */
public interface FormDataScriptingService {

	/**
	 * Выполняет скрипт формы (FormTemplate.script) по определенному событию.
	 * (Для временной поддержки старого механизма при отсутствии FormTemplate.script
	 * отработает старый механизм работы скриптов)
	 *
	 * @param user     текущий пользователь. Вообще, сомнительно его здесь нахождение. Моё мнение: выполднение скриптов
	 *                 не должно зависеть от пользователя.
	 * @param formData данные налоговой формы
	 * @param event    событие формы
	 * @param logger   логгер для сохранения ошибок выполнения скриптов.
	 * @param additionalParameters дополнительные параметры для передачи в скрипты. Их состав зависит от события для которого вызываются
	 *                             скрипты. Параметр может иметь значение null
	 */
	void executeScript(TAUser user, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters);
		

	/**
	 * Проверяет, есть ли скрипты для события формы
	 *
	 * @param formData форма
	 * @param event событие
	 */
	@Deprecated
	boolean hasScripts(FormData formData, FormDataEvent event);
}