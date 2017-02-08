package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;
import java.util.Map;

public class DeleteConfigPropertyAction extends UnsecuredActionImpl<DeleteConfigPropertyResult> {
    private List<Map<String, TableCell>> rows;
    private Long recordId;
    private Integer reportPeriodId;
    private Integer departmentId;
    private Map<String, TableCell> notTableParams;
    private Long refBookId;
    private Long slaveRefBookId;
    private TaxType taxType;

    public List<Map<String, TableCell>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, TableCell>> rows) {
        this.rows = rows;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Map<String, TableCell> getNotTableParams() {
        return notTableParams;
    }

    public void setNotTableParams(Map<String, TableCell> notTableParams) {
        this.notTableParams = notTableParams;
    }

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public Long getSlaveRefBookId() {
        return slaveRefBookId;
    }

    public void setSlaveRefBookId(Long slaveRefBookId) {
        this.slaveRefBookId = slaveRefBookId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
