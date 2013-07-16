package com.aplana.sbrf.taxaccounting.model;

/**
 *  Составная модель подразделения,
 *  включающая в себя общие и частные параметры
 *
 * @author Dmitriy Levykin
 */
public class DepartmentCombined extends Department {

    // Общие параметры
    private DepartmentParam commonParams;
    // Параметры по налогу на прибыль
    private DepartmentParamIncome incomeParams;
    // Параметры по транспортному налогу
    private DepartmentParamTransport transportParams;

    public DepartmentCombined() {
        super();
    }

    public DepartmentCombined(Department dep) {
        super();

        setDepartmentDeclarationTypes(dep.getDepartmentDeclarationTypes());
        setDepartmentFormTypes(dep.getDepartmentFormTypes());
        setDictRegionId(dep.getDictRegionId());
        setId(dep.getId());
        setName(dep.getName());
        setParentId(dep.getParentId());
        setSbrfCode(dep.getSbrfCode());
        setShortName(dep.getShortName());
        setTbIndex(dep.getTbIndex());
        setType(dep.getType());
    }

    public DepartmentParam getCommonParams() {
        return commonParams;
    }

    public void setCommonParams(DepartmentParam commonParams) {
        this.commonParams = commonParams;
    }

    public DepartmentParamIncome getIncomeParams() {
        return incomeParams;
    }

    public void setIncomeParams(DepartmentParamIncome incomeParams) {
        this.incomeParams = incomeParams;
    }

    public DepartmentParamTransport getTransportParams() {
        return transportParams;
    }

    public void setTransportParams(DepartmentParamTransport transportParams) {
        this.transportParams = transportParams;
    }
}
