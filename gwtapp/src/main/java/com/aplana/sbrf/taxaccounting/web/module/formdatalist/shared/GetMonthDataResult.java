package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.gwt.client.Month;
import com.aplana.sbrf.taxaccounting.model.Months;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author fmukhametdinov
 */
public class GetMonthDataResult implements Result {

    private static final long serialVersionUID = 692100272881108671L;

    private boolean isMonthly;

    private List<Months> monthsList;

    public boolean isMonthly() {
        return isMonthly;
    }

    public void setMonthly(boolean isMonthly) {
        this.isMonthly = isMonthly;
    }

    public List<Months> getMonthsList() {
        return monthsList;
    }

    public void setMonthsList(List<Months> monthsList) {
        this.monthsList = monthsList;
    }
}
