package com.aplana.sbrf.taxaccounting.web.widget.version.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetProjectVersion extends UnsecuredActionImpl<GetProjectVersionResult> implements ActionName {

	public GetProjectVersion() {}

	@Override
	public String getName() {
		return String.valueOf("получение версии проекта");
	}
	

}
