package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

public class ReportPeriodViewModel implements Serializable, SecuredEntity {
    private static final long serialVersionUID = 1L;

    /** Уникальный идентификатор отчетного периода */
    private Integer id;
    /** Текстовое обозначение отчетного периода */
    private String name;
    /** Текстовое обозначение отчетного периода для нф с нарастающим итогом*/
    private String accName;
    /** Порядок следования отчетного периода в рамках налового */
    private int order;
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
    private long permissions;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

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

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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

    @Override
    public long getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }


}
