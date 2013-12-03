package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public interface MembersUiHandlers extends UiHandlers {
	void applyFilter();
	void onRangeChange(final int start, int length);
    void onPrintClicked();
}
