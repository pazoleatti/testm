package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Stanislav Yasinskiy
 */
public class LoadRefBookAction extends UnsecuredActionImpl<LoadRefBookResult> implements ActionName {

	@Override
	public String getName() {
		return "Загрузка справочников";
	}

    
}
