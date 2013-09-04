package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Действие сохарнения формы.
 *
 * @author Vitalii Samolovskikh
 */
public class SaveFormDataAction extends AbstractDataRowAction implements ActionName {
	
	@Override
	public String getName() {
		return "Сохранение формы";
	}
}
