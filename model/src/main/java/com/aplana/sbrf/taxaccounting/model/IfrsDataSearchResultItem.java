package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;

/**
 * Данные по отчётности МСФО
 * 
 * @author lhaziev
 *
 */
public class IfrsDataSearchResultItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer reportPeriodId;
	private String blobDataId;
    private Integer year;
    private String periodName;
    //Количество записей
    private int count;

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    public Integer getYear() {
        return year;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}