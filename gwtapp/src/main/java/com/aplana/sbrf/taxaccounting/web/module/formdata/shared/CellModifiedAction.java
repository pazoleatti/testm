package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class CellModifiedAction extends UnsecuredActionImpl<CellModifiedResult> implements ActionName {

	long formDataId;
	List<DataRow<Cell>> dataRows;

	public long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}

	public List<DataRow<Cell>> getDataRows() {
		return dataRows;
	}

	public void setDataRows(List<DataRow<Cell>> dataRows) {
		this.dataRows = dataRows;
	}

	@Override
	public String getName() {
		return "Изменение ячейки";
	}
}
