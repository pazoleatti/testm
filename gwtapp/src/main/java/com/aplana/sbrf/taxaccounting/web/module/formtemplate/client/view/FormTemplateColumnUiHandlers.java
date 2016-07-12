package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateColumnUiHandlers extends UiHandlers {

	void addColumn(Column column);

	void removeColumn(Column column);

	void flushColumn(Column column);

    RefBook getRefBook(Long refBookId);

    RefBookAttribute getRefBookAttribute(Long refBookAttributeId);

    Long getRefBookByAttributeId(Long refBookAttributeId, boolean selectFirstWhenNull);

    int getNextGeneratedColumnId();

    void changeColumnType(int position, Column oldColumn, Column newColumn);

    void onDataViewChanged();
}
