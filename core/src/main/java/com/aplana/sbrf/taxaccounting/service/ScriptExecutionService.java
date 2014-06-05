package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Интерфейс сервиса, реализующего выполение скриптов.
 * @author Stanislav Yasinskiy
 */
public interface ScriptExecutionService {

	/**
	 * Выполняет скрипт по событию TEST.
     *
     * @param userInfo Информация о текущем пользоваетеле.
	 * @param script   выполняемый скрипт.
	 * @param logger   логгер для сохранения ошибок выполнения скриптов.
	 */
	void executeScript(TAUserInfo userInfo, String script, Logger logger);

}