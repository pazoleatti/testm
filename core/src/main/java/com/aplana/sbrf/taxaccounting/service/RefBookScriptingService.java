package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Map;

/**
 * Интерфейс сервиса, реализующего выполнение скриптов справочников
 *
 * @author Dmitriy Levykin
 */
public interface RefBookScriptingService {
    /**
     * Выполнение скрипта отчета по событию
     *
     * @param userInfo Информация о текущем пользователе
     * @param event событие формы
     * @param refBookId Id справочника
     * @param logger логгер для сохранения ошибок выполнения скриптов.
     * @param additionalParameters дополнительные параметры для передачи в скрипты. Их состав зависит от события для которого вызываются
     *                             скрипты. Параметр может иметь значение null
     */
    void executeScript(TAUserInfo userInfo, long refBookId, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters);

    /**
     * Получение скрипта справочника
     *
     * @param refBookId идентификатор справочника
     * @return скрипт, если еще не существует, то пустую строку
     */
    String getScript(Long refBookId);

    /**
     * Сохранение скрипта
     * В зависимости от переданных параметров скрипт может создаваться, обновляться и удаляться
     *
     * @param refBookId идентификатор справочника
     * @param script скрипт
     */
    void saveScript(Long refBookId, String script);
}
