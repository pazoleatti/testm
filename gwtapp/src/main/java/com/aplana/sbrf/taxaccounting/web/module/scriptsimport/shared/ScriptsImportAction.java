package com.aplana.sbrf.taxaccounting.web.module.scriptsimport.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Загрузка бух отчетности на сервер и связь этой бух отчетности с подразделением и отчетным периодом
 * User: ekuvshinov
 */
public class ScriptsImportAction extends UnsecuredActionImpl<ScriptsImportResult> implements ActionName {
    private String uuid;

    @Override
    public String getName() {
        return "Загрузка скриптов";
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
