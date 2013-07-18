package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

import java.util.List;

/**
 * Проверка формы.
 *
 * @author Eugene Stetsenko
 */
public class CheckFormDataAction extends AbstractFormDataAction implements ActionName {

	List<DataRow<Cell>> modifiedRows;

	public List<DataRow<Cell>> getModifiedRows() {
		return modifiedRows;
	}

	public void setModifiedRows(List<DataRow<Cell>> modifiedRows) {
		this.modifiedRows = modifiedRows;
	}

	@Override
	public String getName() {
		return "Обработка запроса на проверку формы";
	}
}
