package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Моделька для бокса с периодами
 *
 * @author dloshkarev
 */
public class PeriodInfo implements HasName, Serializable {
    private static final long serialVersionUID = 342474480237083779L;
    private String code;
    private String name;
    private Date startDate;
    private Date endDate;

    @Override
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
