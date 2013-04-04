package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Действие добавления строки.
 *
 * @author Vitalii Samolovskikh
 */
public class AddRowAction extends AbstractFormDataAction implements ActionName {

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
