package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Конфигурация
 */
@Getter
@Setter
public class Configuration implements Serializable, SecuredEntity {

    private static final long serialVersionUID = -4129485177345542519L;

    private Integer id;
    private String code;
    private String description;
    private Integer departmentId;
    private String value;
    private long permissions;


    public Configuration() {
    }

    public Configuration(String code, Integer departmentId, String value) {
        this.code = code;
        this.departmentId = departmentId;
        this.value = value;
    }

    public Configuration(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
