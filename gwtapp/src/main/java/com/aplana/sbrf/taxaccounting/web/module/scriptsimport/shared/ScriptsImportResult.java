package com.aplana.sbrf.taxaccounting.web.module.scriptsimport.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * Результат загрузки, на текущий момент в случае success придёт пустой результат, в дальнейшем можно сообщать сколько записей импортировали и тд
 * User: ekuvshinov
 */
public class ScriptsImportResult implements Result {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
