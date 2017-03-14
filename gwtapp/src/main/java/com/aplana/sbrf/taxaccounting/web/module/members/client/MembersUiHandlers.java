package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public interface MembersUiHandlers extends UiHandlers {
	void applyFilter();
	void onRangeChange(final int start, int length);
    void onPrintClicked();
    void onSave();
    void setIsFormModified(boolean isFormModified);
    boolean isFormModified();
    TAUserView getSelectUser();
    void setSelectUser(TAUserView user);
}
