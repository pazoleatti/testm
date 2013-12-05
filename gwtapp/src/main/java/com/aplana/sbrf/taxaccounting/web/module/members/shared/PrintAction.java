package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class PrintAction  extends UnsecuredActionImpl<PrintResult> {
	MembersFilterData membersFilterData;

	public MembersFilterData getMembersFilterData() {
		return membersFilterData;
	}

	public void setMembersFilterData(MembersFilterData membersFilterData) {
		this.membersFilterData = membersFilterData;
	}
}
