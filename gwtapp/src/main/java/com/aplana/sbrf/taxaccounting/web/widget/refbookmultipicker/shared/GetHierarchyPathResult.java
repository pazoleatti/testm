package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetHierarchyPathResult implements Result {
    private static final long serialVersionUID = 1099238218534060155L;

    private List<Long> ids;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
