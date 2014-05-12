package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author auldanov
 */
public class SourcesResult implements Result {
    private List<FormToFormRelation> data;

    public List<FormToFormRelation> getData() {
        return data;
    }

    public void setData(List<FormToFormRelation> data) {
        this.data = data;
    }
}
