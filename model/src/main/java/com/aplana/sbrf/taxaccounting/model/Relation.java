package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Моделька для хранения данных по источникам/приемникам
 */
public class Relation implements Serializable {

    /**************  Общие параметры ***************/

    /** подразделение */
    private Department department;
    /** Период */
    private DepartmentReportPeriod departmentReportPeriod;
    /** Статус ЖЦ */
    private WorkflowState state;
    /** форма/декларация создана/не создана */
    private boolean created;
    /** является ли форма источников, в противном случае приемник*/
    private boolean source;
    /** Введена/выведена в/из действие(-ия) */
    private boolean status;
    /** Налог */
    private TaxType taxType;

    /**************  Параметры НФ ***************/

    /** Идентификатор созданной формы */
    private Long formDataId;
    /** Вид НФ */
    private FormType formType;
    /** Тип НФ */
    private FormDataKind formDataKind;
    /** подразделение-исполнитель*/
    private List<Department> performers;
    /** Период сравнения. Может быть null */
    private DepartmentReportPeriod comparativePeriod;
    /** Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
    private boolean accruing;
    /** Номер месяца */
    private Integer month;
    /** Признак ручного ввода */
    private boolean manual;

    /**************  Параметры декларации ***************/

    /** Идентификатор созданной декларации */
    private Long declarationDataId;
    /** Вид декларации */
    private DeclarationTemplate declarationTemplate;
    /** Налоговый орган */
    private String taxOrganCode;
    /** КПП */
    private String kpp;


    /**
     * Статус НФ
     */
    private State declarationState;

    /**
     * Идентификатор АСНУ
     */
    private Long asnuId;


    /**************  Параметры для легкой версии ***************/

    /** Идентификатор подразделения */
    private int departmentId;
    /** полное название подразделения */
    private String fullDepartmentName;
    /** Дата корректировки */
    private Date correctionDate;
    /** Вид нф */
    private String formTypeName;
    /** Вид декларации */
    private String declarationTypeName;
    /** Год налогового периода */
    private int year;
    /** Название периода */
    private String periodName;
    /** Название периода сравнения */
    private String comparativePeriodName;
    /** Дата начала периода сравнения */
    private Date comparativePeriodStartDate;
    /** Год периода сравнения */
    private Integer comparativePeriodYear;
    /** название подразделения-исполнителя */
    private List<String> performerNames;

    public State getDeclarationState() {
        return declarationState;
    }

    public void setDeclarationState(State declarationState) {
        this.declarationState = declarationState;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public DepartmentReportPeriod getDepartmentReportPeriod() {
        return departmentReportPeriod;
    }

    public void setDepartmentReportPeriod(DepartmentReportPeriod departmentReportPeriod) {
        this.departmentReportPeriod = departmentReportPeriod;
    }

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }

    public FormType getFormType() {
        return formType;
    }

    public void setFormType(FormType formType) {
        this.formType = formType;
    }

    public FormDataKind getFormDataKind() {
        return formDataKind;
    }

    public void setFormDataKind(FormDataKind formDataKind) {
        this.formDataKind = formDataKind;
    }

        public DepartmentReportPeriod getComparativePeriod() {
        return comparativePeriod;
    }

    public void setComparativePeriod(DepartmentReportPeriod comparativePeriod) {
        this.comparativePeriod = comparativePeriod;
    }

    public boolean isAccruing() {
        return accruing;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public DeclarationTemplate getDeclarationTemplate() {
        return declarationTemplate;
    }

    public void setDeclarationTemplate(DeclarationTemplate declarationTemplate) {
        this.declarationTemplate = declarationTemplate;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getFullDepartmentName() {
        return fullDepartmentName;
    }

    public void setFullDepartmentName(String fullDepartmentName) {
        this.fullDepartmentName = fullDepartmentName;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

    public String getDeclarationTypeName() {
        return declarationTypeName;
    }

    public void setDeclarationTypeName(String declarationTypeName) {
        this.declarationTypeName = declarationTypeName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getComparativePeriodName() {
        return comparativePeriodName;
    }

    public void setComparativePeriodName(String comparativePeriodName) {
        this.comparativePeriodName = comparativePeriodName;
    }

    public Date getComparativePeriodStartDate() {
        return comparativePeriodStartDate;
    }

    public void setComparativePeriodStartDate(Date comparativePeriodStartDate) {
        this.comparativePeriodStartDate = comparativePeriodStartDate;
    }

    public void setComparativePeriodYear(Integer comparativePeriodYear) {
        this.comparativePeriodYear = comparativePeriodYear;
    }

    public Integer getComparativePeriodYear() {
        return comparativePeriodYear;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public List<Department> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Department> performers) {
        this.performers = performers;
    }

    public List<String> getPerformerNames() {
        return performerNames;
    }

    public void setPerformerNames(List<String> performerNames) {
        this.performerNames = performerNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relation relation = (Relation) o;

        if (created != relation.created) return false;
        if (source != relation.source) return false;
        if (status != relation.status) return false;
        if (accruing != relation.accruing) return false;
        if (manual != relation.manual) return false;
        if (departmentId != relation.departmentId) return false;
        if (year != relation.year) return false;
        if (department != null ? !department.equals(relation.department) : relation.department != null) return false;
        if (departmentReportPeriod != null ? !departmentReportPeriod.equals(relation.departmentReportPeriod) : relation.departmentReportPeriod != null)
            return false;
        if (state != relation.state) return false;
        if (taxType != relation.taxType) return false;
        if (formDataId != null ? !formDataId.equals(relation.formDataId) : relation.formDataId != null) return false;
        if (formType != null ? !formType.equals(relation.formType) : relation.formType != null) return false;
        if (formDataKind != relation.formDataKind) return false;
        if (comparativePeriod != null ? !comparativePeriod.equals(relation.comparativePeriod) : relation.comparativePeriod != null)
            return false;
        if (month != null ? !month.equals(relation.month) : relation.month != null) return false;
        if (declarationDataId != null ? !declarationDataId.equals(relation.declarationDataId) : relation.declarationDataId != null)
            return false;
        if (declarationTemplate != null ? !declarationTemplate.equals(relation.declarationTemplate) : relation.declarationTemplate != null)
            return false;
        if (taxOrganCode != null ? !taxOrganCode.equals(relation.taxOrganCode) : relation.taxOrganCode != null)
            return false;
        if (kpp != null ? !kpp.equals(relation.kpp) : relation.kpp != null) return false;
        if (fullDepartmentName != null ? !fullDepartmentName.equals(relation.fullDepartmentName) : relation.fullDepartmentName != null)
            return false;
        if (correctionDate != null ? !correctionDate.equals(relation.correctionDate) : relation.correctionDate != null)
            return false;
        if (formTypeName != null ? !formTypeName.equals(relation.formTypeName) : relation.formTypeName != null)
            return false;
        if (declarationTypeName != null ? !declarationTypeName.equals(relation.declarationTypeName) : relation.declarationTypeName != null)
            return false;
        if (periodName != null ? !periodName.equals(relation.periodName) : relation.periodName != null) return false;
        if (comparativePeriodName != null ? !comparativePeriodName.equals(relation.comparativePeriodName) : relation.comparativePeriodName != null)
            return false;
        if (comparativePeriodStartDate != null ? !comparativePeriodStartDate.equals(relation.comparativePeriodStartDate) : relation.comparativePeriodStartDate != null)
            return false;
        return comparativePeriodYear != null ? comparativePeriodYear.equals(relation.comparativePeriodYear) : relation.comparativePeriodYear == null;

    }

    @Override
    public int hashCode() {
        int result = department != null ? department.hashCode() : 0;
        result = 31 * result + (departmentReportPeriod != null ? departmentReportPeriod.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (created ? 1 : 0);
        result = 31 * result + (source ? 1 : 0);
        result = 31 * result + (status ? 1 : 0);
        result = 31 * result + (taxType != null ? taxType.hashCode() : 0);
        result = 31 * result + (formDataId != null ? formDataId.hashCode() : 0);
        result = 31 * result + (formType != null ? formType.hashCode() : 0);
        result = 31 * result + (formDataKind != null ? formDataKind.hashCode() : 0);
        result = 31 * result + (comparativePeriod != null ? comparativePeriod.hashCode() : 0);
        result = 31 * result + (accruing ? 1 : 0);
        result = 31 * result + (month != null ? month.hashCode() : 0);
        result = 31 * result + (manual ? 1 : 0);
        result = 31 * result + (declarationDataId != null ? declarationDataId.hashCode() : 0);
        result = 31 * result + (declarationTemplate != null ? declarationTemplate.hashCode() : 0);
        result = 31 * result + (taxOrganCode != null ? taxOrganCode.hashCode() : 0);
        result = 31 * result + (kpp != null ? kpp.hashCode() : 0);
        result = 31 * result + departmentId;
        result = 31 * result + (fullDepartmentName != null ? fullDepartmentName.hashCode() : 0);
        result = 31 * result + (correctionDate != null ? correctionDate.hashCode() : 0);
        result = 31 * result + (formTypeName != null ? formTypeName.hashCode() : 0);
        result = 31 * result + (declarationTypeName != null ? declarationTypeName.hashCode() : 0);
        result = 31 * result + year;
        result = 31 * result + (periodName != null ? periodName.hashCode() : 0);
        result = 31 * result + (comparativePeriodName != null ? comparativePeriodName.hashCode() : 0);
        result = 31 * result + (comparativePeriodStartDate != null ? comparativePeriodStartDate.hashCode() : 0);
        result = 31 * result + (comparativePeriodYear != null ? comparativePeriodYear.hashCode() : 0);
        return result;
    }
}