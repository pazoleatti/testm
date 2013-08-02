package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import com.gwtplatform.dispatch.shared.Result;


public class GetRefBookDereferenceValueResult implements Result {
	private static final long serialVersionUID = 1099858218534060155L;
	
	private String dereferenceValue;

	public String getDereferenceValue() {
		return dereferenceValue;
	}

	public void setDereferenceValue(String dereferenceValue) {
		this.dereferenceValue = dereferenceValue;
	}

}
