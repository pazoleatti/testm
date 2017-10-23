package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Конфигурация
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = -4129485177345542519L;

    private String code;

    private Integer departmentId;

    private String value;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Configuration() {

    }

    public Configuration(String code, Integer departmentId, String value) {
        this.code = code;
        this.departmentId = departmentId;
        this.value = value;
    }
}
