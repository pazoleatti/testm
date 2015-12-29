package com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow;


import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.NotificationTableRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Set;

public interface DialogUiHandlers extends UiHandlers {
	void onRangeChange(int start, int length);

    void deleteNotifications(Set<NotificationTableRow> selectedSet);

    void onEventClick(String uuid);

    void onUrlClick(Long id);
}
