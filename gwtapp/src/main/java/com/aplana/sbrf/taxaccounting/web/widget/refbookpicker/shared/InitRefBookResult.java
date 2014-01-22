package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.util.List;

import com.gwtplatform.dispatch.shared.Result;


public class InitRefBookResult implements Result {
	private static final long serialVersionUID = 1099858218534060155L;
	
	private long refBookId;

	private List<String> headers;

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(long refBookId) {
		this.refBookId = refBookId;
	}
}
