package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetPersonRefBookAttributesResult implements Result {
	private List<RefBookColumn> columns;

    public List<RefBookColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<RefBookColumn> columns) {
        this.columns = columns;
    }
}