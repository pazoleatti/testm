package com.aplana.sbrf.taxaccounting.web.widget.history.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormLogsBusinessAction extends UnsecuredActionImpl<GetLogsBusinessResult> {
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
