package com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetCommonParameterAction extends UnsecuredActionImpl<GetCommonParameterResult> implements ActionName{

	@Override
	public String getName() {
		return "Получение конфигурационных параметров";
	}
}
