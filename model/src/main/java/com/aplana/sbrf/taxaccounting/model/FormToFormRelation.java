package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Моделька для хранения данных по источникам/приемникам
 */
public class FormToFormRelation implements Serializable {
    /** полное название подразделения */
    String fullDepartmentName;

    /** Вид НФ */
    FormType formType;

    /** Тип НФ */
    FormDataKind formDataKind;

    /** Статус */
    WorkflowState state;

    /** подразделение исполнитель*/
    Department performer;

    /** является ли форма источников, в противном случае приемник*/
    boolean source;

    /** форма создана/не создана */
    boolean created;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormToFormRelation)) return false;

        FormToFormRelation that = (FormToFormRelation) o;

        if (created != that.created) return false;
        if (source != that.source) return false;
        if (formDataKind != that.formDataKind) return false;
        if (formType != null ? !formType.equals(that.formType) : that.formType != null) return false;
        if (fullDepartmentName != null ? !fullDepartmentName.equals(that.fullDepartmentName) : that.fullDepartmentName != null)
            return false;
        if (performer != null ? !performer.equals(that.performer) : that.performer != null) return false;
        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fullDepartmentName != null ? fullDepartmentName.hashCode() : 0;
        result = 31 * result + (formType != null ? formType.hashCode() : 0);
        result = 31 * result + (formDataKind != null ? formDataKind.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (performer != null ? performer.hashCode() : 0);
        result = 31 * result + (source ? 1 : 0);
        result = 31 * result + (created ? 1 : 0);
        return result;
    }
}