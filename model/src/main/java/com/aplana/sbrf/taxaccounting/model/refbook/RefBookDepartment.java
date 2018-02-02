package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;

/**
 * Подразделения
 *
 * @author dloshkarev
 */
public class RefBookDepartment extends RefBookSimple<Integer> {
    //Наименование
    private String name;
    //Сокращенное наименование
    private String shortName;
    //Идентификатор родительского подразделения
    private Integer parentId;
    //Тип подразделения (1 - Банк, 2- ТБ, 3- ЦСКО, ПЦП, 4- Управление, 5- Не передается в СУДИР)
    private DepartmentType type;
    //Индекс территориального банка
    private String tbIndex;
    //Код подразделения в нотации Сбербанка
    private String sbrfCode;
    //Код региона
    private Long regionId;
    //Действующее подразделение
    private boolean isActive;
    //Код подразделения
    private Long code;
    //Полное имя подразделения
    private String fullName;

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

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
