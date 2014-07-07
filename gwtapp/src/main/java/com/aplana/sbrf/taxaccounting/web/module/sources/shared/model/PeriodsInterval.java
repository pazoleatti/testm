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
    private int yearFrom;
    private PeriodInfo periodFrom;
    private int yearTo;
    private PeriodInfo periodTo;

    public PeriodsInterval() {
    }

    public PeriodsInterval(Integer yearFrom, PeriodInfo periodFrom, Integer yearTo, PeriodInfo periodTo) {
        this.yearFrom = yearFrom;
        this.periodFrom = periodFrom;
        this.yearTo = yearTo;
        this.periodTo = periodTo;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(int yearFrom) {
        this.yearFrom = yearFrom;
    }

    public String getPeriodStartName() {
        return periodFrom.getName() + " " + yearFrom;
    }

    public String getPeriodEndName() {
        return periodTo.getName() + " " + yearTo;
    }

    public PeriodInfo getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(PeriodInfo periodFrom) {
        this.periodFrom = periodFrom;
    }

    public int getYearTo() {
        return yearTo;
    }

    public void setYearTo(int yearTo) {
        this.yearTo = yearTo;
    }

    public PeriodInfo getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(PeriodInfo periodTo) {
        this.periodTo = periodTo;
    }
}
