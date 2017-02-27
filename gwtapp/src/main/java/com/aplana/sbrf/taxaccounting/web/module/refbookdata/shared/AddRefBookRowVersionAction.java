package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сохранение новой версии записи справочника
 * @author dloshkarev
 */
public class AddRefBookRowVersionAction  extends UnsecuredActionImpl<AddRefBookRowVersionResult> implements ActionName {

    Map<String, RefBookValueSerializable> record;
    long refBookId;
    Long recordId;
    Date versionFrom;
    Date versionTo;
    private Long sourceUniqueRecordId;

    public Map<String, RefBookValueSerializable> getRecord() {
        return record;
    }

    public void setRecord(Map<String, RefBookValueSerializable> record) {
        this.record = record;
    }

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    public Date getVersionFrom() {
        return versionFrom;
    }

    public void setVersionFrom(Date versionFrom) {
        this.versionFrom = versionFrom;
    }

    public Date getVersionTo() {
        return versionTo;
    }

    public void setVersionTo(Date versionTo) {
        this.versionTo = versionTo;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getSourceUniqueRecordId() {
        return sourceUniqueRecordId;
    }

    public void setSourceUniqueRecordId(Long sourceUniqueRecordId) {
        this.sourceUniqueRecordId = sourceUniqueRecordId;
    }

    @Override
    public String getName() {
        return "Добавить версию записи справочника";
    }
}
