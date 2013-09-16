package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;

public class DeleteRefBookRowAction extends UnsecuredActionImpl<DeleteRefBookRowResult> implements ActionName {

	Long refBookId;
	List<Long> recordsId;
	Date relevanceDate;

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

	public Date getRelevanceDate() {
		return relevanceDate;
	}

	public void setRelevanceDate(Date relevanceDate) {
		this.relevanceDate = relevanceDate;
	}

	@Override
	public String getName() {
		return "Удалить запись из справочника";
	}
}
