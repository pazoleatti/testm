package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Months;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author fmukhametdinov
 */
public class GetAdditionalDataResult implements Result {

    private static final long serialVersionUID = 692100272881108671L;

    private boolean isMonthly;
    /** Признак использования периода сравнения (false - не используется, true - используется) */
    private boolean comparative;

    private List<Months> monthsList;
    private List<ReportPeriod> comparativPeriods;

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

    public boolean isComparative() {
        return comparative;
    }

    public void setComparative(boolean comparative) {
        this.comparative = comparative;
    }

    public List<ReportPeriod> getComparativPeriods() {
        return comparativPeriods;
    }

    public void setComparativPeriods(List<ReportPeriod> comparativPeriods) {
        this.comparativPeriods = comparativPeriods;
    }
}
