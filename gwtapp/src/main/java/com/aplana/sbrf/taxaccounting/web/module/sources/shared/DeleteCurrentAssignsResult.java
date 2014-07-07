package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DeleteCurrentAssignsResult  implements Result {
    private static final long serialVersionUID = -4758105347771481207L;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
