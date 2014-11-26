package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Моделька для хранения данных по источникам/приемникам
 */
public class FormToFormRelation implements Serializable {
    /** полное название подразделения */
    private String fullDepartmentName;

    /** Вид НФ */
    private FormType formType;

    /** Тип НФ */
    private FormDataKind formDataKind;

    /** Статус */
    private WorkflowState state;

    /** подразделение исполнитель*/
    private Department performer;

    /** является ли форма источников, в противном случае приемник*/
    private boolean source;

    /** форма создана/не создана */
    private boolean created;

    /** Идентификатор созданной формы */
    private Long formDataId;

    private Date correctionDate;

    private String month;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getFullDepartmentName() {
        return fullDepartmentName;
    }

    public void setFullDepartmentName(String fullDepartmentName) {
        this.fullDepartmentName = fullDepartmentName;
    }

    public Department getPerformer() {
        return performer;
    }

    public void setPerformer(Department performer) {
        this.performer = performer;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
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

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}