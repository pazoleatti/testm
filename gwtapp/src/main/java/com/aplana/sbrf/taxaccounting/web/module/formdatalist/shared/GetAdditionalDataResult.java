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
    /** Признак расчета нарастающим итогом (false - не используется, true - используется)*/
    private boolean accruing;
    private boolean isFirstPeriod;

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

    public boolean isAccruing() {
        return accruing;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }

    public List<ReportPeriod> getComparativePeriods() {
        return comparativPeriods;
    }

    public void setComparativePeriods(List<ReportPeriod> comparativPeriods) {
        this.comparativPeriods = comparativPeriods;
    }

    public boolean isFirstPeriod() {
        return isFirstPeriod;
    }

    public void setFirstPeriod(boolean isFirstPeriod) {
        this.isFirstPeriod = isFirstPeriod;
    }
}
