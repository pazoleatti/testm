package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.Map;

public class SaveRefBookRowVersionAction extends UnsecuredActionImpl<SaveRefBookRowVersionResult> {

	Long refBookId;
	Long recordId;
    Long recordCommonId;
	Map<String, RefBookValueSerializable> valueToSave;
    Date versionFrom;
    Date versionTo;

	public Long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public Map<String, RefBookValueSerializable> getValueToSave() {
		return valueToSave;
	}

	public void setValueToSave(Map<String, RefBookValueSerializable> valueToSave) {
		this.valueToSave = valueToSave;
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

    public Long getRecordCommonId() {
        return recordCommonId;
    }

    public void setRecordCommonId(Long recordCommonId) {
        this.recordCommonId = recordCommonId;
    }
}
