package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

/**
 * @author lhaziev
 */
public class UpdateStatusIrfsDataResult implements Result {

    private Map<Integer, IfrsRow.StatusIfrs> ifrsStatusMap;

    public Map<Integer, IfrsRow.StatusIfrs> getIfrsStatusMap() {
        return ifrsStatusMap;
    }

    public void setIfrsStatusMap(Map<Integer, IfrsRow.StatusIfrs> ifrsStatusMap) {
        this.ifrsStatusMap = ifrsStatusMap;
    }
}