package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.InputStream;

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

    /**
     * Импортирует скриты нф/деклараций/справочников из архива
     * @param zipFile архив со скриптами
     * @param fileName название архива
     */
    void importScripts(Logger logger, InputStream zipFile, String fileName, TAUserInfo userInfo);
}