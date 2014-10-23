package com.aplana.sbrf.taxaccounting.web.module.testpage.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetEventsResult implements Result {
    private static final long serialVersionUID = 1837776652451421385L;

    private Map<Integer, String> map;

    public Map<Integer, String> getMap() {
        return map;
    }

    public void setMap(Map<Integer, String> map) {
        this.map = map;
    }
}
