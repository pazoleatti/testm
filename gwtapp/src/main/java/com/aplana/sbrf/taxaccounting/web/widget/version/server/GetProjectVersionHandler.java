package com.aplana.sbrf.taxaccounting.web.widget.version.server;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetProjectVersionHandler extends
		AbstractActionHandler<GetProjectVersion, GetProjectVersionResult> {

	private static final String RESOURCE_FOR_GETTING_VERSION = "version-info.txt";

	private Log log = LogFactory.getLog(getClass());

	public GetProjectVersionHandler() {
		super(GetProjectVersion.class);
	}

	@Override
	public GetProjectVersionResult execute(GetProjectVersion action,
			ExecutionContext executionContext) throws ActionException {

		
		String version = "unknown";
		String revision = "unknown";
		try {
			InputStream inputStream = this.getClass().getClassLoader()
					.getResourceAsStream(RESOURCE_FOR_GETTING_VERSION);
			Properties prop = new Properties();
			prop.load(inputStream);
			version = prop.getProperty("version", "unknown");
			revision = prop.getProperty("revision","unknown");
		} catch (Exception e) {
			log.error("A error occurred during getting version from resource: "
					+ RESOURCE_FOR_GETTING_VERSION, e);
		}
		
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
