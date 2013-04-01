package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateColumnUiHandlers extends UiHandlers {

	void addColumn(int position, Column column);

	void addColumn(Column column);

	void removeColumn(Column column);

	void flushColumn(Column column);
}
