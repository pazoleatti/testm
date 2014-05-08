package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetRowsDataAction extends UnsecuredActionImpl<GetRowsDataResult> implements ActionName {

	DataRowRange range;
	boolean readOnly;
    boolean manual;
	long formDataId;
	int formDataTemplateId;
	List<DataRow<Cell>> modifiedRows;
    String innerLogUuid;

    public String getInnerLogUuid() {
        return innerLogUuid;
    }

    public void setInnerLogUuid(String innerLogUuid) {
        this.innerLogUuid = innerLogUuid;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public DataRowRange getRange() {
		return range;
	}

	public void setRange(DataRowRange range) {
		this.range = range;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}

	public int getFormDataTemplateId() {
		return formDataTemplateId;
	}

	public void setFormDataTemplateId(int formDataTemplateId) {
		this.formDataTemplateId = formDataTemplateId;
	}

	public List<DataRow<Cell>> getModifiedRows() {
		return modifiedRows;
	}

	public void setModifiedRows(List<DataRow<Cell>> modifiedRows) {
		this.modifiedRows = modifiedRows;
	}

	@Override
	public String getName() {
		return "Получить строки";
	}
}
