package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;

import java.io.Serializable;

/**
 * Объединенная модель назначений НФ или деклараций подразделению
 * @author aivanov
 */
public class DepartmentAssign implements Serializable {
	private static final long serialVersionUID = -5523995354908631670L;
	
	private Long id;
	private int departmentId;
	private int typeId;
	private String typeName;
	private FormDataKind kind;
    private boolean isForm = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public FormDataKind getKind() {
        return kind;
    }

    public void setKind(FormDataKind kind) {
        this.kind = kind;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }
}
