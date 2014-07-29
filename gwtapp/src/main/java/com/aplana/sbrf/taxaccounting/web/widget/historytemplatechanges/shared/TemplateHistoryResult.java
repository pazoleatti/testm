package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
public abstract class TemplateHistoryResult implements Result {
    private static final long serialVersionUID = 7326537883004803479L;
    private List<TemplateChangesExt> changesExtList;

    public List<TemplateChangesExt> getChangesExtList() {
        return changesExtList;
    }

    public void setChangesExtList(List<TemplateChangesExt> changesExtList) {
        this.changesExtList = changesExtList;
    }
}
