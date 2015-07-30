package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Set;

/**
 * User: avanteev
 */
public class DeleteJrxmlAction extends UnsecuredActionImpl<DeleteJrxmlResult> {
    /**
     * Идентификаторы для очистки
     */
    private Set<Long> ids;
    private Set<Long> lockIds;
    private int dtId;

    public int getDtId() {
        return dtId;
    }

    public void setDtId(int dtId) {
        this.dtId = dtId;
    }

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

    public Set<Long> getLockIds() {
        return lockIds;
    }

    public void setLockIds(Set<Long> lockIds) {
        this.lockIds = lockIds;
    }
}
