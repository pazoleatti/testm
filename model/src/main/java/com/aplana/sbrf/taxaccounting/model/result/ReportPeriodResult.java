package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

import java.util.Date;

/**
 * Содержит и переносит данные об отчетном периоде
 */
public class ReportPeriodResult {

    /** Уникальный идентификатор отчетного периода */
    private Integer id;
    /** Текстовое обозначение отчетного периода */
    private String name;
    /** Ссылка на налоговый период */
    private TaxPeriod taxPeriod;
    /** Дата начала отчетного периода */
    private Date startDate;
    /** Дата окончания отчетного периода */
    private Date endDate;
    /** Календарная дата начала отчетного периода (квартала) */
    private Date calendarStartDate;
    /** Ссылка на федеральный справочника для классификации отчетных периодов */
    private long dictTaxPeriodId;

    private Date correctionDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaxPeriod getTaxPeriod() {
        return taxPeriod;
    }

    public void setTaxPeriod(TaxPeriod taxPeriod) {
        this.taxPeriod = taxPeriod;
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

    public Date getCalendarStartDate() {
        return calendarStartDate;
    }

    public void setCalendarStartDate(Date calendarStartDate) {
        this.calendarStartDate = calendarStartDate;
    }

    public long getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public void setDictTaxPeriodId(long dictTaxPeriodId) {
        this.dictTaxPeriodId = dictTaxPeriodId;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
