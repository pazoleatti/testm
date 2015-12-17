package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

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
    private Department performer;
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
    private DeclarationType declarationType;
    /** Налоговый орган */
    private String taxOrganCode;
    /** КПП */
    private String kpp;

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
    private String performerName;

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

    public Department getPerformer() {
        return performer;
    }

    public void setPerformer(Department performer) {
        this.performer = performer;
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

    public DeclarationType getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(DeclarationType declarationType) {
        this.declarationType = declarationType;
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

    public String getPerformerName() {
        return performerName;
    }

    public void setPerformerName(String performerName) {
        this.performerName = performerName;
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

    @Override
    public String toString() {
        return "Relation{" +
                "department=" + department +
                ", departmentReportPeriod=" + departmentReportPeriod +
                ", state=" + state +
                ", created=" + created +
                ", source=" + source +
                ", status=" + status +
                ", formDataId=" + formDataId +
                ", formType=" + formType +
                ", formDataKind=" + formDataKind +
                ", performer=" + performer +
                ", comparativePeriod=" + comparativePeriod +
                ", accruing=" + accruing +
                ", month=" + month +
                ", manual=" + manual +
                ", declarationDataId=" + declarationDataId +
                ", declarationType=" + declarationType +
                ", taxOrganCode='" + taxOrganCode + '\'' +
                ", kpp='" + kpp + '\'' +
                ", departmentId=" + departmentId +
                ", fullDepartmentName='" + fullDepartmentName + '\'' +
                ", correctionDate=" + correctionDate +
                ", formTypeName='" + formTypeName + '\'' +
                ", declarationTypeName='" + declarationTypeName + '\'' +
                ", year=" + year +
                ", periodName='" + periodName + '\'' +
                ", comparativePeriodName='" + comparativePeriodName + '\'' +
                ", comparativePeriodStartDate=" + comparativePeriodStartDate +
                ", comparativePeriodYear=" + comparativePeriodYear +
                ", performerName='" + performerName + '\'' +
                '}';
    }
}