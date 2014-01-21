package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetDTVersionListResult implements Result {
    private List<DeclarationTemplateVersion> templateVersions;

    public List<DeclarationTemplateVersion> getTemplateVersions() {
        return templateVersions;
    }

    public void setTemplateVersions(List<DeclarationTemplateVersion> templateVersions) {
        this.templateVersions = templateVersions;
    }
}
