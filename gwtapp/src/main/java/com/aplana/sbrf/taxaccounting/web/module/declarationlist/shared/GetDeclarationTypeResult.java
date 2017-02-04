package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.List;

public class GetDeclarationTypeResult implements Result {
    private static final long serialVersionUID = 6048433881484626479L;

    private List<DeclarationType> declarationTypes;
    private Date correctionDate;
    private TaxType taxType;
    private Date version;

    private String filter;

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public List<DeclarationType> getDeclarationTypes() {
        return declarationTypes;
    }

    public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
        this.declarationTypes = declarationTypes;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
