package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.gwtplatform.dispatch.shared.Result;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class GetMembersResult implements Result {

    private PagingResult<TAUserView> taUserList;
    private int startIndex;

    public PagingResult<TAUserView> getTaUserList() {
        return taUserList;
    }

    public void setTaUserList(PagingResult<TAUserView> taUserList) {
        this.taUserList = taUserList;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
