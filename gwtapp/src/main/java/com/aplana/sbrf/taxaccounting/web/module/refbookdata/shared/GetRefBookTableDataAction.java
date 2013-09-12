package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class GetRefBookTableDataAction extends UnsecuredActionImpl<GetRefBookTableDataResult> implements ActionName {

	long refbookId;
	PagingParams pagingParams;
	Date relevanceDate;

	public long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(long refbookId) {
		this.refbookId = refbookId;
	}

	public PagingParams getPagingParams() {
		return pagingParams;
	}

	public void setPagingParams(PagingParams pagingParams) {
		this.pagingParams = pagingParams;
	}

	public Date getRelevanceDate() {
		return relevanceDate;
	}

	public void setRelevanceDate(Date relevanceDate) {
		this.relevanceDate = relevanceDate;
	}

	@Override
	public String getName() {
		return "Получить строку из справочника";
	}
}
