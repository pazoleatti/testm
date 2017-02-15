package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Set;

public class SaveAction extends UnsecuredActionImpl<GetTableDataResult> implements ActionName{

    public SaveAction() {
    }

    private Set<Long> ids;
    private Long departmentId;
    private Integer typeId;
    private Integer formId;
    private char taxType;
    private boolean isForm;

    public char getTaxType() {
        return taxType;
    }

    public void setTaxType(char taxType) {
        this.taxType = taxType;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean form) {
        isForm = form;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

	@Override
	public String getName() {
		StringBuilder sb = new StringBuilder();
		if (ids == null){
			sb.append("Добавление назначений ");
		} else {
			sb.append("Удаление назначений ");
		}
		sb.append("налоговых форм");
		return sb.toString();
	}
}
