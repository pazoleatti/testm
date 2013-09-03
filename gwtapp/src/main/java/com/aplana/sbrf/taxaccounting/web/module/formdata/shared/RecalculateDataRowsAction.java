package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Пересчет формы.
 *
 * @author Eugene Stetsenko
 * @author Vitalii Samolovskikh
 */
public class RecalculateDataRowsAction extends AbstractDataRowAction implements ActionName {

	@Override
	public String getName() {
		return "Пересчет данных формы";
	}
}
