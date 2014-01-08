package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Date;

/**
 * Данные о версии элемента справочника
 * @author dloshkarev
 */
public class RefBookRecordVersion implements Serializable {
    private static final long serialVersionUID = -3193428262749898993L;

    /** Уникальный идентификатор записи справочника. Поле Id таблицы ref_book_record */
    private Long recordId;

    /** Дата начала действия версии */
    private Date versionStart;

    /** Дата конца действия версии */
    private Date versionEnd;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Date getVersionStart() {
        return versionStart;
    }

    public void setVersionStart(Date versionStart) {
        this.versionStart = versionStart;
    }

    public Date getVersionEnd() {
        return versionEnd;
    }

    public void setVersionEnd(Date versionEnd) {
        this.versionEnd = versionEnd;
    }

    @Override
    public String toString() {
        return "RefBookRecordVersion{" +
                "recordId=" + recordId +
                ", versionStart=" + versionStart +
                ", versionEnd=" + versionEnd +
                '}';
    }
}
