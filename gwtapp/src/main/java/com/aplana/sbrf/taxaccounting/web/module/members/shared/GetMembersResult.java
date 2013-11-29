package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class GetMembersResult implements Result {

    private PagingResult<TAUserFull> taUserList;
	private int startIndex;
	private List<Department> departments;

	public PagingResult<TAUserFull> getTaUserList() {
		return taUserList;
	}

	public void setTaUserList(PagingResult<TAUserFull> taUserList) {
		this.taUserList = taUserList;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}
}
