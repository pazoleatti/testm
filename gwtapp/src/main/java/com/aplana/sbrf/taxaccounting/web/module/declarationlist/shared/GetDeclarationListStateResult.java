package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.State;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetDeclarationListStateResult implements Result {

    private Map<Long, State> stateMap;
    private String uuid;

    public Map<Long, State> getStateMap() {
        return stateMap;
    }

    public void setStateMap(Map<Long, State> stateMap) {
        this.stateMap = stateMap;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
