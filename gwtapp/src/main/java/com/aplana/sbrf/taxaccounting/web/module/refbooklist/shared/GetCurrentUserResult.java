package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.gwtplatform.dispatch.shared.Result;

public class GetCurrentUserResult implements Result {
    TAUser user;

    public TAUser getUser() {
        return user;
    }

    public void setUser(TAUser user) {
        this.user = user;
    }
}
