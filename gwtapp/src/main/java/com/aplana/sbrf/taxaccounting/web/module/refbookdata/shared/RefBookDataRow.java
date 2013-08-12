package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.io.Serializable;
import java.util.Map;

public class RefBookDataRow implements Serializable {
	Map<String, RefBookTableCell> values;

	public Map<String, RefBookTableCell> getValues() {
		return values;
	}

	public void setValues(Map<String, RefBookTableCell> values) {
		this.values = values;
	}
}
