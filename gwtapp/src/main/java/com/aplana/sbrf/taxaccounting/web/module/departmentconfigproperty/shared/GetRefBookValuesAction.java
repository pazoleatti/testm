package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetRefBookValuesAction extends UnsecuredActionImpl<GetRefBookValuesResult> {
    Long refBookId;
    Long slaveRefBookId;
    Integer reportPeriodId;
    Integer departmentId;

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
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

    public Long getSlaveRefBookId() {
        return slaveRefBookId;
    }

    public void setSlaveRefBookId(Long slaveRefBookId) {
        this.slaveRefBookId = slaveRefBookId;
    }
}
