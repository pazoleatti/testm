package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetRefBookValuesResult implements Result {
    List<Map<String, TableCell>> tableValues;
    Map<String, TableCell> notTableValues;
    Long recordId;

    public List<Map<String, TableCell>> getTableValues() {
        return tableValues;
    }

    public void setTableValues(List<Map<String, TableCell>> tableValues) {
        this.tableValues = tableValues;
    }

    public Map<String, TableCell> getNotTableValues() {
        return notTableValues;
    }

    public void setNotTableValues(Map<String, TableCell> notTableValues) {
        this.notTableValues = notTableValues;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
