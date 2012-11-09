package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class StringColumn extends Column  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String dictionaryCode;

	public String getDictionaryCode() {
		return dictionaryCode;
	}

	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}
}
