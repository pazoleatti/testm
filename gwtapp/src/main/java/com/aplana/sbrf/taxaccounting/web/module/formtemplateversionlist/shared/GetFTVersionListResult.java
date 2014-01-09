package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetFTVersionListResult implements Result {
    private List<FormTemplateVersion> formTemplateVersions;

    public List<FormTemplateVersion> getFormTemplateVersions() {
        return formTemplateVersions;
    }

    public void setFormTemplateVersions(List<FormTemplateVersion> formTemplateVersions) {
        this.formTemplateVersions = formTemplateVersions;
    }
}
