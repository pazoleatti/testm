package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author fmukhametdinov
 */
public class GetMonthData extends UnsecuredActionImpl<GetMonthDataResult> {

    private Integer typeId;

    private Integer periodId;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }
}
