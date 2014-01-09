package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetNameResult implements Result {
	String name;

    /**
     * Значения уникальных атрибутов записи справочника.
     * Используется только в версионировании
     */
    String uniqueAttributeValues;

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
}
