package com.aplana.sbrf.taxaccounting.model.migration;

import java.io.Serializable;
import java.util.Date;

public class Exemplar implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long exemplarId;

    private Integer periodityId;
    private Date beginDate;
    private Date endDate;

    private Integer rnuTypeId;
    private String depCode;
    private Integer systemId;
    private String subSystemId;

    private String terCode;

    public Exemplar() {
    }

    public Long getExemplarId() {
        return exemplarId;
    }

    public void setExemplarId(Long exemplarId) {
        this.exemplarId = exemplarId;
    }

    public Integer getPeriodityId() {
        return periodityId;
    }

    public void setPeriodityId(Integer periodityId) {
        this.periodityId = periodityId;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getRnuTypeId() {
        return rnuTypeId;
    }

    public void setRnuTypeId(Integer rnuTypeId) {
        this.rnuTypeId = rnuTypeId;
    }

    public String getDepCode() {
        return depCode;
    }

    public void setDepCode(String depCode) {
        this.depCode = depCode;
    }

    public Integer getSystemId() {
        return systemId;
    }

    public void setSystemId(Integer systemId) {
        this.systemId = systemId;
    }

    public String getSubSystemId() {
        return subSystemId;
    }

    public void setSubSystemId(String subSystemId) {
        this.subSystemId = subSystemId;
    }

    public String getTerCode() {
        return terCode;
    }

    public void setTerCode(String terCode) {
        this.terCode = terCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Exemplar");
        sb.append("{Id=").append(exemplarId);
        sb.append(", periodityId=").append(periodityId);
        sb.append(", beginDate=").append(beginDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", rnuTypeId=").append(rnuTypeId);
        sb.append(", depCode='").append(depCode).append('\'');
        sb.append(", systemId=").append(systemId);
        sb.append(", subSystemId='").append(subSystemId).append('\'');
        sb.append(", terCode='").append(terCode).append('\'');
        return sb.toString();
    }
}

