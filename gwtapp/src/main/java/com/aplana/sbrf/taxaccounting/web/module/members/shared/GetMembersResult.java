package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserFullWithDepartmentPath;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class GetMembersResult implements Result {

    private PagingResult<TAUserFullWithDepartmentPath> taUserList;
	private int startIndex;

	public PagingResult<TAUserFullWithDepartmentPath> getTaUserList() {
		return taUserList;
	}

	public void setTaUserList(PagingResult<TAUserFullWithDepartmentPath> taUserList) {
		this.taUserList = taUserList;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
}
