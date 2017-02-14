package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.Map;

public class GetSubreportResult implements Result {
    private static final long serialVersionUID = 7859961980147513071L;

    private Map<Long, RefBookParamInfo> refBookParamInfoMap;
    private Date startDate;
    private Date endDate;
    private boolean selectRecord;

    public Map<Long, RefBookParamInfo> getRefBookParamInfoMap() {
        return refBookParamInfoMap;
    }

    public void setRefBookParamInfoMap(Map<Long, RefBookParamInfo> refBookParamInfoMap) {
        this.refBookParamInfoMap = refBookParamInfoMap;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isSelectRecord() {
        return selectRecord;
    }

    public void setSelectRecord(boolean isSelectedRecord) {
        this.selectRecord = isSelectedRecord;
    }
}
