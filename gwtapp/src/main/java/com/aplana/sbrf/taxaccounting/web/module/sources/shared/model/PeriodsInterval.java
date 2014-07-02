package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import java.io.Serializable;

/**
 * Модель для хранения состояния выбранного интервала периодов
 *
 * @author aivanov
 * @since 28.05.2014
 */
public class PeriodsInterval implements Serializable {
    private static final long serialVersionUID = 7617988671379164269L;
    private Integer yearFrom;
    private PeriodInfo periodFrom;
    private Integer yearTo;
    private PeriodInfo periodTo;

    public PeriodsInterval() {
    }

    public PeriodsInterval(Integer yearFrom, PeriodInfo periodFrom, Integer yearTo, PeriodInfo periodTo) {
        this.yearFrom = yearFrom;
        this.periodFrom = periodFrom;
        this.yearTo = yearTo;
        this.periodTo = periodTo;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        this.yearFrom = yearFrom;
    }

    public PeriodInfo getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(PeriodInfo periodFrom) {
        this.periodFrom = periodFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public void setYearTo(Integer yearTo) {
        this.yearTo = yearTo;
    }

    public PeriodInfo getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(PeriodInfo periodTo) {
        this.periodTo = periodTo;
    }
}
