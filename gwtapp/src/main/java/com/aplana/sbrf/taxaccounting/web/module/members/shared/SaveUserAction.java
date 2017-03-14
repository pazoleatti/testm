package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class SaveUserAction extends UnsecuredActionImpl<SaveUserResult> {
	private TAUserView taUserView;

    public TAUserView getTaUserView() {
        return taUserView;
    }

    public void setTaUserView(TAUserView taUserView) {
        this.taUserView = taUserView;
    }
}
