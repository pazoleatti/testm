package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

public class DeleteRowAction extends AbstractFormDataAction implements ActionName {

	private DataRow deletedDataRow;

	public DataRow getDeletedDataRow() {
		return deletedDataRow;
	}

	public void setDeletedDataRow(DataRow deletedDataRow) {
		this.deletedDataRow = deletedDataRow;
	}

	@Override
	public String getName() {
		return "Удаление строки на форме";
	}

}
