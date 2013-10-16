package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class SaveConfigurationAction extends UnsecuredActionImpl<SaveConfigurationResult> implements ActionName{
	
	private List<ConfigTuple> data;

	public List<ConfigTuple> getData() {
		return data;
	}

	public void setData(List<ConfigTuple> data) {
		this.data = data;
	}
	
	@Override
	public String getName() {
		return "Получение конфигурационных параметров";
	}
	
}
