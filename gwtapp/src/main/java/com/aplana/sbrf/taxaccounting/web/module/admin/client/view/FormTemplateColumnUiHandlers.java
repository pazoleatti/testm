package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateColumnUiHandlers extends UiHandlers {
	void addColumn(Column column);

	void removeColumn(Column column);

	void flashColumn(Column column);
}
