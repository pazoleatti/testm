package com.aplana.sbrf.taxaccounting.web.module.userlist.shared;

import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetUserListResult implements Result {

    private List<TAUserFull> taUserList;

    public List<TAUserFull> getTaUserList() {
        return taUserList;
    }

    public void setTaUserList(List<TAUserFull> taUserList) {
        this.taUserList = taUserList;
    }
}
