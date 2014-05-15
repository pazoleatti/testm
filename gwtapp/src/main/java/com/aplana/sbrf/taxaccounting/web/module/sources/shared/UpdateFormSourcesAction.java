package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class UpdateFormSourcesAction extends UnsecuredActionImpl<UpdateSourcesResult> {
	private Long departmentFormTypeId;
	private List<Long> sourceDepartmentFormTypeIds;
    private PeriodInfo periodFrom;
    private PeriodInfo periodTo;
    private int yearFrom;
    private int yearTo;

    public PeriodInfo getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(PeriodInfo periodFrom) {
        this.periodFrom = periodFrom;
    }

    public PeriodInfo getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(PeriodInfo periodTo) {
        this.periodTo = periodTo;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(int yearFrom) {
        this.yearFrom = yearFrom;
    }

    public int getYearTo() {
        return yearTo;
    }

    public void setYearTo(int yearTo) {
        this.yearTo = yearTo;
    }

    public Long getDepartmentFormTypeId() {
		return departmentFormTypeId;
	}

	public void setDepartmentFormTypeId(Long departmentFormTypeId) {
		this.departmentFormTypeId = departmentFormTypeId;
	}

	public List<Long> getSourceDepartmentFormTypeIds() {
		return sourceDepartmentFormTypeIds;
	}

	public void setSourceDepartmentFormTypeIds(List<Long> sourceDepartmentFormTypeIds) {
		this.sourceDepartmentFormTypeIds = sourceDepartmentFormTypeIds;
	}
}
