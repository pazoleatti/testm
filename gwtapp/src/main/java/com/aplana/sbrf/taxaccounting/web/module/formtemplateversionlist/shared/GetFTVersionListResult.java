package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetFTVersionListResult implements Result {
    private List<FormTemplateVersion> formTemplateVersions;
    private String formTypeName;
    private TaxType taxType;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

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
