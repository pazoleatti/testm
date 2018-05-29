package com.aplana.sbrf.taxaccounting.model.refbook;

import java.util.Date;

/**
 * Класс для версионируемых справочников
 * @author dloshkarev
 */
public abstract class RefBookVersioned<IdType extends Number> extends RefBookSimple<IdType> {
    /** Идентификатор группы версий */
    private long recordId;
    /** Дата начала действия версии */
    private Date version;
    /** Дата окончания действия версии. Может быть null - тогда дата окончания не задана */
    private Date versionEnd;

    public Date getVersionEnd() {
        return versionEnd;
    }

    public void setVersionEnd(Date versionEnd) {
        this.versionEnd = versionEnd;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
