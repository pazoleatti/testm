package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetFTVersionListResult implements Result {
    private List<FormTemplateVersion> formTemplateVersions;
    private String formTypeName;

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

    public List<FormTemplateVersion> getFormTemplateVersions() {
        return formTemplateVersions;
    }

    public void setFormTemplateVersions(List<FormTemplateVersion> formTemplateVersions) {
        this.formTemplateVersions = formTemplateVersions;
    }
}
