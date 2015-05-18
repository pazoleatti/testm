package com.aplana.sbrf.taxaccounting.model.source;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;

import java.util.Date;

/**
 * Информация о консолидируемой нф/декларации
 * @author dloshkarev
 */
public class ConsolidatedInstance {
    private String type;
    private FormDataKind formKind;
    private String department;
    private String period;
    private Date correctionDate;
    private boolean declaration;

    public boolean isDeclaration() {
        return declaration;
    }

    public void setDeclaration(boolean declaration) {
        this.declaration = declaration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FormDataKind getFormKind() {
        return formKind;
    }

    public void setFormKind(int formKind) {
        this.formKind = FormDataKind.fromId(formKind);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
