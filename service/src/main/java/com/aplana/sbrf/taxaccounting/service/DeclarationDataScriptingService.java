package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;

/**
 * Интерфейс сервиса для запуска скриптов по декларациями (пока существует только один скрипт - скрипт создания)
 * @author dsultanbekov
 */
public interface DeclarationDataScriptingService {

	/**
	 * Вызов скрипта для формирования xml
	 *
	 * @param logger объект для вывод диагностических сообщений
	 * @param declarationData объект-декларация
	 * @param docDate дата обновления декларации
	 * @return xml-данные, формат законодателя
	 */
	String create(Logger logger, DeclarationData declarationData, String docDate);

	/**
	 * Вызов скрипта для осуществления логических проверок при переводе декларации из статуса "Создана" в "Принята"
	 *
	 * @param logger объект для вывод диагностических сообщений
	 * @param declarationData объект-декларация
	 */
	void accept(Logger logger, DeclarationData declarationData);
}
