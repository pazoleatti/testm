package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUser;

/**
 * Интерфейс сервиса, реализующего выполение скриптов над данными налоговых форм.
 */
public interface FormDataScriptingService {
	/**
	 * Выполняет скрипты формы по определенному событию.
	 *
	 * @param user     текущий пользователь. Вообще, сомнительно его здесь нахождение. Моё мнение: выполднение скриптов
	 *                 не должно зависеть от пользователя.
	 * @param formData данные налоговой формы
	 * @param event    событие формы
	 * @param logger   логгер для сохранения ошибок выполнения скриптов.
	 * @param additionalParameter	дополнительные параметры, если нет то null
	 */
	void executeScripts(TAUser user, FormData formData, FormDataEvent event, Logger logger,Object additionalParameter);

	/**
	 * Проверяет, есть ли скрипты для события формы
	 *
	 * @param formData форма
	 * @param event событие
	 */
	boolean hasScripts(FormData formData, FormDataEvent event);
}