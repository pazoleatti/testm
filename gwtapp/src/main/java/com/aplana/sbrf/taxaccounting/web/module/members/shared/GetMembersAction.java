package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class GetMembersAction extends UnsecuredActionImpl<GetMembersResult> {

	MembersFilterData membersFilterData;

	public MembersFilterData getMembersFilterData() {
		return membersFilterData;
	}

	public void setMembersFilterData(MembersFilterData membersFilterData) {
		this.membersFilterData = membersFilterData;
	}
}
