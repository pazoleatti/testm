package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import java.io.Serializable;
import java.util.Date;

public class PeriodInfo implements Serializable {
    private static final long serialVersionUID = 342474480237083779L;
    private String name;
    private Date startDate;
    private Date endDate;

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
}
