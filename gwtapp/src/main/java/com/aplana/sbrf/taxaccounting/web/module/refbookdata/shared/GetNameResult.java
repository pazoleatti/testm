package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetNameResult implements Result {
	String name;

    /**
     * Значения уникальных атрибутов записи справочника.
     * Используется только в версионировании
     */
    String uniqueAttributeValues;

    /** Идентификатор текущей записи без учета версий (тип записи) */
    Long recordId;

    private Integer RefBookType;

	public String getName() {
		return name;
	}

    public void setName(String name) {
		this.name = name;
	}

    public String getUniqueAttributeValues() {
        return uniqueAttributeValues;
    }

    public void setUniqueAttributeValues(String uniqueAttributeValues) {
        this.uniqueAttributeValues = uniqueAttributeValues;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Integer getRefBookType() {
        return RefBookType;
    }

    public void setRefBookType(Integer refBookType) {
        RefBookType = refBookType;
    }
}
