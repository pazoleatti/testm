package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class DeleteRefBookRowAction extends UnsecuredActionImpl<DeleteRefBookRowResult> implements ActionName {

	Long refBookId;
	List<Long> recordsId;
    /** Признак того, что удаляется одна версия, а не все версии записи */
    boolean deleteVersion;

    public boolean isDeleteVersion() {
        return deleteVersion;
    }

    public void setDeleteVersion(boolean deleteVersion) {
        this.deleteVersion = deleteVersion;
    }

    public Long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	public List<Long> getRecordsId() {
		return recordsId;
	}

	public void setRecordsId(List<Long> recordsId) {
		this.recordsId = recordsId;
	}

	@Override
	public String getName() {
		return "Удалить запись из справочника";
	}
}
