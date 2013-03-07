package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.icommon.ActionName;

/**
 * Пересчет формы.
 *
 * @author Eugene Stetsenko
 * @author Vitalii Samolovskikh
 */
public class RecalculateFormDataAction extends AbstractFormDataAction implements ActionName {

	@Override
	public String getName() {
		return "Пересчет формы";
	}
}
