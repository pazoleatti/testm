package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

import java.util.List;

/**
 * Пересчет формы.
 *
 * @author Eugene Stetsenko
 * @author Vitalii Samolovskikh
 */
public class RecalculateFormDataAction extends AbstractFormDataAction implements ActionName {

	List<DataRow<Cell>> modifiedRows;

	public List<DataRow<Cell>> getModifiedRows() {
		return modifiedRows;
	}

	public void setModifiedRows(List<DataRow<Cell>> modifiedRows) {
		this.modifiedRows = modifiedRows;
	}

	@Override
	public String getName() {
		return "Пересчет формы";
	}
}
