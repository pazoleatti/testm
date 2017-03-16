package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Map;

/**
 * Запуск асинх задачи формирования отчетов
 *
 * @author lhaziev
 */
public class CreateReportAction extends UnsecuredActionImpl<CreateReportResult> implements ActionName {

    private long declarationDataId;
    private boolean isForce;
    private TaxType taxType;
    private String type;
    private Map<String, Object> subreportParamValues;
    private DataRow<Cell> selectedRow;
    private boolean create;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
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

    public DataRow<Cell> getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(DataRow<Cell> selectedRow) {
        this.selectedRow = selectedRow;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    @Override
	public String getName() {
		return "Формирование отчетов";
	}
}
