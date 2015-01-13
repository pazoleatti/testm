package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.io.Serializable;
import java.util.Map;

public class RefBookDataRow implements Serializable {
	Long refBookRowId;
	Map<String, String> values;

	public Long getRefBookRowId() {
		return refBookRowId;
	}

	public void setRefBookRowId(Long refBookRowId) {
		this.refBookRowId = refBookRowId;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefBookDataRow that = (RefBookDataRow) o;

        return refBookRowId.equals(that.refBookRowId);

    }
}
