package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model;

import java.io.Serializable;

/**
 * Общая модель для нижней таблицы
 * @author sgoryachkin
 * @author aivanov
 */
public class IfrsRow implements Serializable{

    public static enum StatusIfrs {
        EXIST("Сформирован"), //существует
        LOCKED("Инициировано формирование"), //есть блокировка
        NOT_EXIST("Не сформирован"); //не существует

        private String name;
        StatusIfrs(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private Integer reportPeriodId;
    private StatusIfrs status;
    private Integer year;
    private String periodName;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public StatusIfrs getStatus() {
        return status;
    }

    public void setStatus(StatusIfrs status) {
        this.status = status;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }
}
