package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.io.Serializable;
import java.util.Map;

public class RefBookDataRow implements Serializable {
    /**
     * Уникальной идентификатор из таблицы ref_book_record
     */
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

	@Override
	public int hashCode() {
		int result = refBookRowId != null ? refBookRowId.hashCode() : 0;
		result = 31 * result + (values != null ? values.hashCode() : 0);
		return result;
	}
}
