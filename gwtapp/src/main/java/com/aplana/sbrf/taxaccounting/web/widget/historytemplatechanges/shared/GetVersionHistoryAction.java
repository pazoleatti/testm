package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared;

import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.VersionHistoryPresenter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetVersionHistoryAction extends UnsecuredActionImpl<GetVersionHistoryResult> {

    private int typeId;
    private VersionHistoryPresenter.TemplateType templateType;

    public VersionHistoryPresenter.TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(VersionHistoryPresenter.TemplateType templateType) {
        this.templateType = templateType;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
