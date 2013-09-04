package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

public class DeleteRowAction extends AbstractDataRowAction implements ActionName {

	private DataRow<Cell> currentDataRow;

	public DataRow<Cell> getCurrentDataRow() {
		return currentDataRow;
	}

	public void setCurrentDataRow(DataRow<Cell> currentDataRow) {
		this.currentDataRow = currentDataRow;
	}

	@Override
	public String getName() {
		return "Удаление строки из формы";
	}

}