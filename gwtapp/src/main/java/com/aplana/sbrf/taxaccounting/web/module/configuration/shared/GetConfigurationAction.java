package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetConfigurationAction extends UnsecuredActionImpl<GetConfigurationResult> implements ActionName{

	@Override
	public String getName() {
		return "Получение конфигурационных параметров";
	}
	

	
}
