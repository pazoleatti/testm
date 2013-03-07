package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Действие добавления строки.
 *
 * @author Vitalii Samolovskikh
 */
public class AddRowAction extends AbstractFormDataAction implements ActionName {

	@Override
	public String getName() {
		return "Добавление строки на форме";
	}
}
