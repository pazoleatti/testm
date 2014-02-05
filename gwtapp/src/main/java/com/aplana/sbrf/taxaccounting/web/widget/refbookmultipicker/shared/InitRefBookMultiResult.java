package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import java.util.List;
import java.util.Map;

import com.gwtplatform.dispatch.shared.Result;


public class InitRefBookMultiResult implements Result {
	private static final long serialVersionUID = 1099858218534060155L;
	
	private long refBookId;

	private Map<String, Integer> headers;

	public Map<String, Integer> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Integer> headers) {
		this.headers = headers;
	}

	public long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(long refBookId) {
		this.refBookId = refBookId;
	}
}
