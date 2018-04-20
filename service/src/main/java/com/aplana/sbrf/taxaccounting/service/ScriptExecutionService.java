package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.io.InputStream;

/**
 * Интерфейс сервиса, реализующего выполение скриптов.
 *
 * @author Stanislav Yasinskiy
 */
public interface ScriptExecutionService {

    /**
     * Извлекает содержимое скрипта из архива
     *
     * @param scriptArchive архив со скриптом
     */
    String extractScript(InputStream scriptArchive);

    /**
     * Извлекает скрипт из архива и выполняет его
     *
     * @param userInfo Информация о текущем пользоваетеле.
     * @param script   скрипт
     */
    ActionResult executeScript(TAUserInfo userInfo, String script);

    /**
     * Импортирует скриты нф/деклараций/справочников из архива
     *
     * @param zipFile  архив со скриптами
     * @param fileName название архива
     */
    void importScripts(Logger logger, InputStream zipFile, String fileName, TAUserInfo userInfo);
}