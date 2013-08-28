package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;

/**
 * @author sgoryachkin
 *
 */
public class DepartmentFormTypeShared implements Serializable{
	private static final long serialVersionUID = -5564995354908631670L;
	
	private boolean checked;
	private int index;
	
	private Long id;
	private String departmentName;
	private String formTypeName;
	private FormDataKind kind;
	
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

	public String getFormTypeName() {
		return formTypeName;
	}

	public void setFormTypeName(String formTypeName) {
		this.formTypeName = formTypeName;
	}

	public FormDataKind getKind() {
		return kind;
	}

	public void setKind(FormDataKind kind) {
		this.kind = kind;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
