package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.icommon.ActionName;

/**
 * Проверка формы.
 *
 * @author Eugene Stetsenko
 */
public class CheckFormDataAction extends AbstractFormDataAction implements ActionName {

	@Override
	public String getName() {
		return "Обработка запроса на проверку формы";
	}
}
