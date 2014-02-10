package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.gwtplatform.dispatch.shared.Result;

public class GetNotificationsResult implements Result {
	private static final long serialVersionUID = -6138788436672890473L;

	PagingResult<NotificationTableRow> rows;

	public PagingResult<NotificationTableRow> getRows() {
		return rows;
	}

	public void setRows(PagingResult<NotificationTableRow> rows) {
		this.rows = rows;
	}
}
