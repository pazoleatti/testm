package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.TemplateChangesExt;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetVersionHistoryResult implements Result {
    private List<TemplateChangesExt> changes;

    public List<TemplateChangesExt> getChanges() {
        return changes;
    }

    public void setChanges(List<TemplateChangesExt> changes) {
        this.changes = changes;
    }
}
