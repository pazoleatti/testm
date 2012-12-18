package com.aplana.sbrf.taxaccounting.web.main.page.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetProjectVersionResult  implements Result {

	public GetProjectVersionResult(){}

	private String projectVersion;

	public String getProjectVersion() {
		return projectVersion;
	}

	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}

}
