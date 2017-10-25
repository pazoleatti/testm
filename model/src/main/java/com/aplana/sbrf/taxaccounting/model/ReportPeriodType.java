package com.aplana.sbrf.taxaccounting.model;


import org.joda.time.LocalDateTime;

import java.io.Serializable;


public class ReportPeriodType implements Serializable{

    /** Уникальный идентификатор отчетного периода */
    private Long id;

    /** Текстовое обозначение отчетного периода */
    private String name;

    /** Дата начала отчетного периода */
    private LocalDateTime startDate;

    /** Дата окончания отчетного периода */
    private LocalDateTime endDate;

    /** Календарная дата начала отчетного периода (квартала) */
    private LocalDateTime calendarStartDate;

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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCalendarStartDate() {
        return calendarStartDate;
    }

    public void setCalendarStartDate(LocalDateTime calendarStartDate) {
        this.calendarStartDate = calendarStartDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
