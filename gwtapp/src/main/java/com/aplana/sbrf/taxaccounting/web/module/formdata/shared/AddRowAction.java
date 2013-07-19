package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

import java.util.List;

/**
 * Действие добавления строки.
 *
 * @author Vitalii Samolovskikh
 */
public class AddRowAction extends AbstractFormDataAction implements ActionName {

	List<DataRow<Cell>> modifiedRows;

	public List<DataRow<Cell>> getModifiedRows() {
		return modifiedRows;
	}

	public void setModifiedRows(List<DataRow<Cell>> modifiedRows) {
		this.modifiedRows = modifiedRows;
	}

	private DataRow currentDataRow;

	public DataRow getCurrentDataRow() {
		return currentDataRow;
	}

	public void setCurrentDataRow(DataRow currentDataRow) {
		this.currentDataRow = currentDataRow;
	}

	@Override
	public String getName() {
		return "Добавление строки в форму";
	}
}
