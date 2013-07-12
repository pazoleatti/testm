package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetRowsDataAction extends UnsecuredActionImpl<GetRowsDataResult> implements ActionName {

	DataRowRange range;
	long formDataId;
	List<DataRow<Cell>> modifiedRows;

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

	public List<DataRow<Cell>> getModifiedRows() {
		return modifiedRows;
	}

	public void setModifiedRows(List<DataRow<Cell>> modifiedRows) {
		this.modifiedRows = modifiedRows;
	}

	@Override
	public String getName() {
		return "Получить строки";
	}
}
