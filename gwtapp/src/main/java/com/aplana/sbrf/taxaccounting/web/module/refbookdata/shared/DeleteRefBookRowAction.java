package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class DeleteRefBookRowAction extends UnsecuredActionImpl<DeleteRefBookRowResult> implements ActionName {

	Long refbookId;
	List<Long> recordsId;

	public Long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(Long refbookId) {
		this.refbookId = refbookId;
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
