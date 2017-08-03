package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class CheckReceiversAcceptedPreparedResult implements Result {

    List<Long> receiversAcceptedPreparedIdList;

    public List<Long> getReceiversAcceptedPreparedIdList() {
        return receiversAcceptedPreparedIdList;
    }

    public void setReceiversAcceptedPreparedIdList(List<Long> receiversAcceptedPreparedIdList) {
        this.receiversAcceptedPreparedIdList = receiversAcceptedPreparedIdList;
    }
}
