package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetRefBookAttributesResult implements Result {
	List<RefBookColumn> columns;
    private boolean readOnly;
    private Integer refBookType;
    private boolean sendQuery;
    private List<String> specificReportTypes;

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public List<RefBookColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<RefBookColumn> columns) {
		this.columns = columns;
	}

    public Integer getRefBookType() {
        return refBookType;
    }

    public void setRefBookType(Integer refBookType) {
        this.refBookType = refBookType;
    }

    public boolean isSendQuery() {
        return sendQuery;
    }

    public void setSendQuery(boolean sendQuery) {
        this.sendQuery = sendQuery;
    }

    public List<String> getSpecificReportTypes() {
        return specificReportTypes;
    }

    public void setSpecificReportTypes(List<String> specificReportTypes) {
        this.specificReportTypes = specificReportTypes;
    }
}
