package com.aplana.sbrf.taxaccounting.web.widget.version.server;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetProjectVersionHandler extends
		AbstractActionHandler<GetProjectVersion, GetProjectVersionResult> {

	@Autowired
	private Properties manifestProperties;

	public GetProjectVersionHandler() {
		super(GetProjectVersion.class);
	}

	@Override
	public GetProjectVersionResult execute(GetProjectVersion action,
			ExecutionContext executionContext) throws ActionException {

		
		String version = manifestProperties.getProperty("Implementation-Version", "unknown");
		String revision = manifestProperties.getProperty("X-Git-Build-Number-And-Date", "unknown");

		
		GetProjectVersionResult result = new GetProjectVersionResult();
		result.setProjectVersion(version + ", Ревизия: " + revision);
		return result;

	}

	@Override
	public void undo(GetProjectVersion getProjectVersion,
			GetProjectVersionResult getProjectVersionResult,
			ExecutionContext executionContext) throws ActionException {
		// ничего не делаем
	}
}
