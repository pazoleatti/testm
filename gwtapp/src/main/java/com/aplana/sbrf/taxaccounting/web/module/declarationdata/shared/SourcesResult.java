package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.Relation;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author auldanov
 */
public class SourcesResult implements Result {
    private List<Relation> data;

    public List<Relation> getData() {
        return data;
    }

    public void setData(List<Relation> data) {
        this.data = data;
    }
}
