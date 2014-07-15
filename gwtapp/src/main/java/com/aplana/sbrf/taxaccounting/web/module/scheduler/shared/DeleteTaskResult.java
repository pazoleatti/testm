package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * Результат удаления задачи
 * @author dloshkarev
 */
public class DeleteTaskResult implements Result {
    private static final long serialVersionUID = 1457691267349523170L;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
