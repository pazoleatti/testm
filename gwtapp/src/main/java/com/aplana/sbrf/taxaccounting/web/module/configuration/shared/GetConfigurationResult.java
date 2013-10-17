package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import java.util.List;

import com.gwtplatform.dispatch.shared.Result;

public class GetConfigurationResult implements Result {
	private static final long serialVersionUID = 4328465660539237715L;
	
	private List<ConfigTuple> data;

	public List<ConfigTuple> getData() {
		return data;
	}

	public void setData(List<ConfigTuple> data) {
		this.data = data;
	}

}