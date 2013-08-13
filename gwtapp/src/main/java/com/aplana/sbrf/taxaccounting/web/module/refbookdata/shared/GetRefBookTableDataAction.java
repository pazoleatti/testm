package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetRefBookTableDataAction extends UnsecuredActionImpl<GetRefBookTableDataResult> implements ActionName {

	long refbookId;

	public long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(long refbookId) {
		this.refbookId = refbookId;
	}

	@Override
	public String getName() {
		return "";
	}
}
