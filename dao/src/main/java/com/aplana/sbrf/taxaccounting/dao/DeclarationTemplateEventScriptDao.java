package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;

import java.util.List;

/**
 * dao для работы со скриптами событий {@link DeclarationTemplateEventScript}
 */
public interface DeclarationTemplateEventScriptDao {

    /**
     * Найти скрипты для событий привязанные к определенному макету
     * @param declarationTemplateId
     * @return
     */
    List<DeclarationTemplateEventScript> fetch(int declarationTemplateId);

    /**
     * Получить скрипт по его id в БД
     * @param declarationTemplateEventScriptId
     * @return
     */
    String getScript(long declarationTemplateEventScriptId);

    /**
     * Найти скрипт привязанный к определенному макету и событию
     * @param declarationTemplateId
     * @param eventId
     * @return
     */
    String findScript(int declarationTemplateId, int eventId);

    /**
     * Обновить содержимое скрипта
     * @param declarationTemplateEventScriptId
     * @param script
     */
    void updateScript(long declarationTemplateEventScriptId, String script);

    /**
     * Проверить существование скрипта
     * @param declarationTemplateId
     * @param formDataEventId
     * @return
     */
    boolean checkIfEventScriptPresent(int declarationTemplateId, int formDataEventId);

    /**
     * Сохранить новый скрипт в БД
     * @param declarationTemplateEventScript
     * @return
     */
    DeclarationTemplateEventScript create(DeclarationTemplateEventScript declarationTemplateEventScript);

    /**
     * Удалить скрипт
     * @param declarationTemplateEventScriptId
     */
    void delete(long declarationTemplateEventScriptId);

    /**
     * Сравнивает состояние списка скриптов для макета с состоянием в бд: в случае несоответствия, изменяет список объектов
     * в БД - удаляет удаленные, сохраняет новые, обновляет существующие.
     * @param declarationTemplate
     */
    void updateScriptList(DeclarationTemplate declarationTemplate);
}
