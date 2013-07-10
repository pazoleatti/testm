package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

public interface SourcesUiHandlers extends UiHandlers {
	void accept();
	void setSources(int departmentId, TaxType taxType);
}
