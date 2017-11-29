package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.Map;

public class CreateReportAction {
    private long declarationDataId;
    private boolean isForce;
    private TaxType taxType;
    private String type;
    private boolean create;
    private Map<String, Object> subreportParamValues;
    private Map<String, Object> selectedRow;

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
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

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public long getDeclarationDataId() {

        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public Map<String, Object> getSubreportParamValues() {
        return subreportParamValues;
    }

    public void setSubreportParamValues(Map<String, Object> subreportParamValues) {
        this.subreportParamValues = subreportParamValues;
    }

    public Map<String, Object> getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(Map<String, Object> selectedRow) {
        this.selectedRow = selectedRow;
    }
}
