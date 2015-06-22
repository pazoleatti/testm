package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetDTVersionListResult implements Result {
    private List<DeclarationTemplateVersion> templateVersions;
    private String dtTypeName;
    private TaxType taxType;


    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public String getDtTypeName() {
        return dtTypeName;
    }

    public void setDtTypeName(String dtTypeName) {
        this.dtTypeName = dtTypeName;
    }

    public List<DeclarationTemplateVersion> getTemplateVersions() {
        return templateVersions;
    }

    public void setTemplateVersions(List<DeclarationTemplateVersion> templateVersions) {
        this.templateVersions = templateVersions;
    }
}
