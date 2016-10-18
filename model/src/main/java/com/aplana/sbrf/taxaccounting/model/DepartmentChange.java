package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Изменение справочника "Подразделения"
 * 
 * @author lhaziev
 */
public class DepartmentChange implements Serializable {
	private static final long serialVersionUID = 1965168313168404199L;

    private DepartmentChangeOperationType operationType;
    private int departmentId;
    private Date logDate;
    private Integer level;
	private String name;
	private String shortName;
	private Integer parentId;
	private DepartmentType type;
	private String tbIndex;
	private String sbrfCode;
    private String region;
    private Boolean isActive;
    private Long code;
    private Boolean garantUse;
    private Boolean sunrUse;

    public DepartmentChangeOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(DepartmentChangeOperationType operationType) {
        this.operationType = operationType;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public DepartmentType getType() {
        return type;
    }

    public void setType(DepartmentType type) {
        this.type = type;
    }

    public String getTbIndex() {
        return tbIndex;
    }

    public void setTbIndex(String tbIndex) {
        this.tbIndex = tbIndex;
    }

    public String getSbrfCode() {
        return sbrfCode;
    }

    public void setSbrfCode(String sbrfCode) {
        this.sbrfCode = sbrfCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Boolean getGarantUse() {
        return garantUse;
    }

    public void setGarantUse(Boolean garantUse) {
        this.garantUse = garantUse;
    }

    public Boolean getSunrUse() {
        return sunrUse;
    }

    public void setSunrUse(Boolean sunrUse) {
        this.sunrUse = sunrUse;
    }
}