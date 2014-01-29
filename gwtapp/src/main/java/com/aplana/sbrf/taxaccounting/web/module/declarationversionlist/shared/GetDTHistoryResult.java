package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.TemplateChangesExt;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetDTHistoryResult implements Result {
    private List<TemplateChangesExt> templateChangesExts;

    public List<TemplateChangesExt> getTemplateChangesExts() {
        return templateChangesExts;
    }

    public void setTemplateChangesExts(List<TemplateChangesExt> templateChangesExts) {
        this.templateChangesExts = templateChangesExts;
    }
}
