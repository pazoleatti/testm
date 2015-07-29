package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Set;

/**
 * User: avanteev
 */
public class CheckJrxmlResult implements Result {
    private boolean canDelete;
    /**
     * Идентификаторы для очистки
     */
    private Set<Long> ids;
    private Set<Long> lockIds;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<Long> getLockIds() {
        return lockIds;
    }

    public void setLockIds(Set<Long> lockIds) {
        this.lockIds = lockIds;
    }

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}
