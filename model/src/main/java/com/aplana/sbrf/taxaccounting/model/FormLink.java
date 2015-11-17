package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Нф в которой обнаружено использование записи справочника
 * @author dloshkarev
 */
public class FormLink implements Serializable {
    private static final long serialVersionUID = -3358747826995397702L;

    /** идентификатор нф */
    private long formDataId;
    /** вид нф */
    private int formTypeId;
    /** состояние ЖЦ нф */
    private WorkflowState state;
    /** сообщение об обнаружении ссылки в нф */
    private String msg;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormLink formLink = (FormLink) o;

        if (formDataId != formLink.formDataId) return false;
        if (formTypeId != formLink.formTypeId) return false;
        if (msg != null ? !msg.equals(formLink.msg) : formLink.msg != null) return false;
        if (state != formLink.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (formDataId ^ (formDataId >>> 32));
        result = 31 * result + formTypeId;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }
}
