package com.aplana.sbrf.taxaccounting.web.widget.version.server;

import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GetProjectVersionHandler extends AbstractActionHandler<GetProjectVersion, GetProjectVersionResult> {

    @Autowired
    private ApplicationInfo applicationInfo;

	@Autowired
	private ServerInfo serverInfo;

	public GetProjectVersionHandler() {
		super(GetProjectVersion.class);
	}

	@Override
	public GetProjectVersionResult execute(GetProjectVersion action, ExecutionContext executionContext) throws ActionException {
		
		GetProjectVersionResult result = new GetProjectVersionResult();
		result.setProjectVersion(String.format("Версия: %s; Ревизия: %s; Сервер: %s", applicationInfo.getVersion(), applicationInfo.getRevision(), serverInfo.getServerName()));
        return result;

	}

	@Override
	public void undo(GetProjectVersion getProjectVersion,
			GetProjectVersionResult getProjectVersionResult,
			ExecutionContext executionContext) throws ActionException {
		// ничего не делаем
	}

	public Map<String, String> getProjectVersionProperties(){
		Map<String, String> result = new HashMap<String, String>();
		result.put("version", applicationInfo.getVersion());
		result.put("revision", applicationInfo.getRevision());
		result.put("serverName", serverInfo.getServerName());
		return result;
	}
}
