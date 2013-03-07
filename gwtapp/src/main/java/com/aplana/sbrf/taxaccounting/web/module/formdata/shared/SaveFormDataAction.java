package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.icommon.ActionName;

/**
 * Действие сохарнения формы.
 *
 * @author Vitalii Samolovskikh
 */
public class SaveFormDataAction extends AbstractFormDataAction implements ActionName {

	@Override
	public String getName() {
		return "\"Сохранение формы\"";
	}
}
