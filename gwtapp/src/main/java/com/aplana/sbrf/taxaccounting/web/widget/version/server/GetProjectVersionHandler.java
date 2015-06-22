package com.aplana.sbrf.taxaccounting.web.widget.version.server;

import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import com.ibm.websphere.runtime.ServerName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class GetProjectVersionHandler extends
		AbstractActionHandler<GetProjectVersion, GetProjectVersionResult> {

    @Autowired
    @Qualifier("versionInfoProperties")
    private Properties versionInfoProperties;

	public GetProjectVersionHandler() {
		super(GetProjectVersion.class);
	}

	@Override
	public GetProjectVersionResult execute(GetProjectVersion action,
			ExecutionContext executionContext) throws ActionException {
		
		String version = "?";
		String revision = "?";

        if (versionInfoProperties != null) {
            version = versionInfoProperties.getProperty("version");
            revision = versionInfoProperties.getProperty("revision");
        }
		
		GetProjectVersionResult result = new GetProjectVersionResult();
        result.setProjectVersion(String.format("Версия: %s, Ревизия: %s, Сервер: %s", version, revision, ServerName.getDisplayName()));
        return result;

	}

	@Override
	public void undo(GetProjectVersion getProjectVersion,
			GetProjectVersionResult getProjectVersionResult,
			ExecutionContext executionContext) throws ActionException {
		// ничего не делаем
	}
}
