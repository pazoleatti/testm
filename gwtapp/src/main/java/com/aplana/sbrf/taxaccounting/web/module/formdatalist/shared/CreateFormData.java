package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateFormData extends UnsecuredActionImpl<CreateFormDataResult> implements ActionName {

	private Integer formDataTypeId;

	private Integer formDataKindId;

    private Integer monthId;

    private Integer departmentReportPeriodId;

	public Integer getFormDataTypeId() {
		return formDataTypeId;
	}

	public void setFormDataTypeId(Integer formDataTypeId) {
		this.formDataTypeId = formDataTypeId;
	}

	public Integer getFormDataKindId() {
		return formDataKindId;
	}

	public void setFormDataKindId(Integer formDataKindId) {
		this.formDataKindId = formDataKindId;
	}

    public Integer getMonthId() {
        return monthId;
    }

    public void setMonthId(Integer monthId) {
        this.monthId = monthId;
    }

    public Integer getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(Integer departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    @Override
	public String getName() {
		return "Создание налоговой формы";
	}
}
