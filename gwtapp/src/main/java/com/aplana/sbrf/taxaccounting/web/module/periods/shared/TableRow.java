package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import java.io.Serializable;
import java.util.Date;

public class TableRow implements Serializable {

	private int id;
	private int reportPeriodId;
	private int departmentId;
	private String periodKind;
	private String periodName;
	private Boolean periodCondition;
	private boolean isSubHeader;
    private Boolean balance;
	int year; //TODO Возможно не  нужно хранить дату в каждом экземпляре
    private long dictTaxPeriodId;
    private int ord; // Порядок следования в налоговом периоде
    private int departmentReportPeriodId;

    public int getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(int departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	private Date deadline;
    private Date correctPeriod;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    public long getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public String getPeriodKind() {
		return periodKind;
	}

	public void setPeriodKind(String periodKind) {
		this.periodKind = periodKind;
	}

	public String getPeriodName() {
		return periodName;
	}

	public void setPeriodName(String periodName) {
		this.periodName = periodName;
	}

	public Boolean isOpen() {
		return periodCondition;
	}

	public void setPeriodCondition(Boolean periodCondition) {
		this.periodCondition = periodCondition;
	}

	public boolean isSubHeader() {
		return isSubHeader;
	}

	public void setSubHeader(boolean subHeader) {
		isSubHeader = subHeader;
	}

    public Boolean isBalance() {
        return balance;
    }

    public void setBalance(Boolean balance) {
        this.balance = balance;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getCorrectPeriod() {
        return correctPeriod;
    }

    public void setCorrectPeriod(Date correctPeriod) {
        this.correctPeriod = correctPeriod;
    }

    public long getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public void setDictTaxPeriodId(long dictTaxPeriodId) {
        this.dictTaxPeriodId = dictTaxPeriodId;
    }

    public int getOrd() {
        return ord;
    }

    public void setOrd(int ord) {
        this.ord = ord;
    }

    public boolean isCorrection(){
        return correctPeriod != null;
    }
}
