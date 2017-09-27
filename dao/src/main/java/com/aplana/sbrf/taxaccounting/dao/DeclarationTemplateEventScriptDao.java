package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;

import java.util.List;

public interface DeclarationTemplateEventScriptDao {

    /**
     * Найти скрипты для событий привязанные к определенному макету
     * @param declarationTemplateId
     * @return
     */
    public List<DeclarationTemplateEventScript> fetch(int declarationTemplateId);

    public String getScript(long declarationTemplateEventScriptId);

    public String findScript(int declarationTemplateId, int eventId);

    public void updateScript(long declarationTemplateEventScriptId, String script);

    /**
     * Проверить существование скрипта
     * @param declarationTemplateId
     * @param formDataEventId
     * @return
     */
    public boolean checkIfEventScriptPresent(int declarationTemplateId, int formDataEventId);

    DeclarationTemplateEventScript create(DeclarationTemplateEventScript declarationTemplateEventScript);

    void delete(long declarationTemplateEventScriptId);

    void updateScriptList(DeclarationTemplate declarationTemplate);
}
