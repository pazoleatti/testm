package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetDeclarationListResult implements Result {
	private static final long serialVersionUID = 783660987579644789L;

	private List<DeclarationDataSearchResultItem> records;

    Map<Integer, String> departmentFullNames;

	//общее количество записей (на всех страницах)
	private long totalCountOfRecords;

    private Integer Page;

	public GetDeclarationListResult() {

	}

	public List<DeclarationDataSearchResultItem> getRecords() {
		return records;
	}

	public void setRecords(List<DeclarationDataSearchResultItem> records) {
		this.records = records;
	}

    public Map<Integer, String> getDepartmentFullNames() {
        return departmentFullNames;
    }

    public void setDepartmentFullNames(Map<Integer, String> departmentFullNames) {
        this.departmentFullNames = departmentFullNames;
    }

    public long getTotalCountOfRecords() {
		return totalCountOfRecords;
	}

	public void setTotalCountOfRecords(long totalCountOfRecords) {
		this.totalCountOfRecords = totalCountOfRecords;
	}

    public Integer getPage() {
        return Page;
    }

    public void setPage(Integer page) {
        Page = page;
    }
}
