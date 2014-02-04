package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.gwtplatform.dispatch.shared.Result;


public class GetRefMultiBookValuesResult implements Result {
	private static final long serialVersionUID = 1099858218534060155L;
	
	private PagingResult<RefBookItem> page;

	public PagingResult<RefBookItem> getPage() {
		return page;
	}

	public void setPage(PagingResult<RefBookItem> page) {
		this.page = page;
	}

}
