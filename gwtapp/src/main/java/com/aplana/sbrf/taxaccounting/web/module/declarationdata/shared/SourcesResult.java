package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.RelationViewModel;

import java.util.List;

/**
 * @author auldanov
 */
public class SourcesResult extends DeclarationDataResult {
    private List<RelationViewModel> data;

    public List<RelationViewModel> getData() {
        return data;
    }

    public void setData(List<RelationViewModel> data) {
        this.data = data;
    }
}
