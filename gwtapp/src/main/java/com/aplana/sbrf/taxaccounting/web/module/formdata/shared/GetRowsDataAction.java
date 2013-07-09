package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetRowsDataAction extends UnsecuredActionImpl<GetRowsDataResult> implements ActionName {

	DataRowRange range;
	long formDataId;

	public DataRowRange getRange() {
		return range;
	}

	public void setRange(DataRowRange range) {
		this.range = range;
	}

	public long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}

	@Override
	public String getName() {
		return "Получить строки";
	}
}
