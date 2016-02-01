package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Общая модель для нижней таблицы
 * @author sgoryachkin
 * @author aivanov
 */
public class CurrentAssign implements Serializable{
	private static final long serialVersionUID = -5564995354908631670L;

    /* Идентификатор */
    private Long id;
    private int departmentId;
    /* Название подразделения */
    private String departmentName;

    /* Название вида налоговой формы или декларации */
    private String name;

    /* Название типа НФ */
    private FormDataKind formKind;
    /* Дата начала назначения */
    private Date startDateAssign;
    /* Дата окончания */
    private Date endDateAssign;

    private FormType formType;
    private DeclarationType declarationType;
    private TaxType taxType;
    private Boolean isForm;
    private boolean enabled = true;

    public DeclarationType getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(DeclarationType declarationType) {
        this.declarationType = declarationType;
    }


    public FormType getFormType() {
        return formType;
    }

    public void setFormType(FormType formType) {
        this.formType = formType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDateAssign() {
        return startDateAssign;
    }

    public void setStartDateAssign(Date startDateAssign) {
        this.startDateAssign = startDateAssign;
    }

    public Date getEndDateAssign() {
        return endDateAssign;
    }

    public void setEndDateAssign(Date endDateAssign) {
        this.endDateAssign = endDateAssign;
    }

    public FormDataKind getFormKind() {
        return formKind;
    }

    public void setFormKind(FormDataKind formKind) {
        this.formKind = formKind;
    }

    public Boolean getIsForm() {
        return isForm;
    }

    public void setIsForm(Boolean isForm) {
        this.isForm = isForm;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
