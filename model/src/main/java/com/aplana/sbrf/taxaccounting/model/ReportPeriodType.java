package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;
import java.util.Date;

/**
 * Модель для типа отчтеного периода из справочника
 */
public class ReportPeriodType implements Serializable{

    /** Уникальный идентификатор отчетного периода */
    private Long id;

    /** Текстовое обозначение отчетного периода */
    private String name;

    /** Дата начала отчетного периода */
    private Date startDate;

    /** Дата окончания отчетного периода */
    private Date endDate;

    /** Календарная дата начала отчетного периода (квартала) */
    private Date calendarStartDate;

    /** Код типа периода*/
    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
