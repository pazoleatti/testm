package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.Map;

public class PrepareSubreportAction {

    private long declarationDataId;
    private TaxType taxType;
    private String type;
    private Map<String, Object> subreportParamValues;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getSubreportParamValues() {
        return subreportParamValues;
    }

    public void setSubreportParamValues(Map<String, Object> subreportParamValues) {
        this.subreportParamValues = subreportParamValues;
    }
}
