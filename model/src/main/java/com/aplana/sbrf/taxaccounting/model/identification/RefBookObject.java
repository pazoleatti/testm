package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.Date;

/**
 * @author Andrey Drunk
 */
public class RefBookObject extends IdentityObject<Long> {

    /**
     *
     */
    protected Date version;

    /**
     *
     */
    protected Integer status;

    /**
     * Идентификатор записи в справочнике, заполняется при получении записи из БД
     */
    protected Long recordId;

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
