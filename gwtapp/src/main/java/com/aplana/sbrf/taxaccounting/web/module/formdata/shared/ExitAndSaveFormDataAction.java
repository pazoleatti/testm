package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Действие "Выход (без отмены операций)"
 *
 * @author lhaziev
 */
public class ExitAndSaveFormDataAction extends AbstractDataRowAction implements ActionName {
	
	@Override
	public String getName() {
		return "Выход (без отмены операций)";
	}
}
