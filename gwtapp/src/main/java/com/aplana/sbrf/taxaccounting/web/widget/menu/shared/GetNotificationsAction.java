package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetNotificationsAction extends UnsecuredActionImpl<GetNotificationsResult> {
	int start;
	int length;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}
