package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.gwtplatform.dispatch.shared.Result;


public class GetRefBookMultiDereferenceValueResult implements Result {
	private static final long serialVersionUID = 1099858218534060155L;
	
	private String dereferenceValue;

	public String getDereferenceValue() {
		return dereferenceValue;
	}

	public void setDereferenceValue(String dereferenceValue) {
		this.dereferenceValue = dereferenceValue;
	}

}
