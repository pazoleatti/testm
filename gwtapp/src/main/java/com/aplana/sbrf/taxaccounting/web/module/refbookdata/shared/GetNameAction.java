package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetNameAction extends UnsecuredActionImpl<GetNameResult> implements ActionName {
	Long refbookId;

	public Long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(Long refbookId) {
		this.refbookId = refbookId;
	}

	@Override
	public String getName() {
		return "Получить название справочника";
	}
}
