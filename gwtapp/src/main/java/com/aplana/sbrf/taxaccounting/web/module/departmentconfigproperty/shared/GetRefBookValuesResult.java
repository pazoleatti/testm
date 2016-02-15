package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class GetRefBookValuesResult implements Result {
    List<Map<String, TableCell>> tableValues;
    Map<String, TableCell> notTableValues;
    Long recordId;
    private String uuid;
    private String errorMsg;
    private Date configStartDate;
    private Date configEndDate;

    public Date getConfigEndDate() {
        return configEndDate;
    }

    public void setConfigEndDate(Date configEndDate) {
        this.configEndDate = configEndDate;
    }

    public void setConfigStartDate(Date configStartDate) {
        this.configStartDate = configStartDate;
    }

    public Date getConfigStartDate() {
        return configStartDate;
    }

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
