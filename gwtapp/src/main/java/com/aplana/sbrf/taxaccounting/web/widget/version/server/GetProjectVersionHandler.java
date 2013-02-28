package com.aplana.sbrf.taxaccounting.web.widget.version.server;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetProjectVersionHandler extends AbstractActionHandler<GetProjectVersion,GetProjectVersionResult> {

	public GetProjectVersionHandler() {
		super(GetProjectVersion.class);
	}

	protected Log logger = LogFactory.getLog(getClass());

	@Override
	public GetProjectVersionResult execute(GetProjectVersion action, ExecutionContext executionContext) throws ActionException {
		InputStream inputStream = getClass().getResourceAsStream("/com/aplana/sbrf/taxaccounting/version.txt");
		String version = "unknown";
		if(inputStream != null){
			try {
				version = IOUtils.toString(inputStream) ;
			} catch (IOException e) {
				logger.error("A error occurred during getting version from resource: /com/aplana/sbrf/taxaccounting/version.txt");
			}
		}

		GetProjectVersionResult result = new GetProjectVersionResult();
		result.setProjectVersion(version);
		return result;
	}

	@Override
	public void undo(GetProjectVersion getProjectVersion, GetProjectVersionResult getProjectVersionResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
