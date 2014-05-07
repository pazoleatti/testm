package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetNotificationsAction extends UnsecuredActionImpl<GetNotificationsResult> {

    public GetNotificationsAction() {
    }

    public GetNotificationsAction(NotificationsFilterData filter) {
        this.filter = filter;
    }

    private NotificationsFilterData filter;

    public NotificationsFilterData getFilter() {
        return filter;
    }

    public void setFilter(NotificationsFilterData filter) {
        this.filter = filter;
    }
}
