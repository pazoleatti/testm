package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Сохранение перед поиском в форме.
 *
 * @author Bulat Kinzyabulatov
 */
public class PreSearchAction extends UnsecuredActionImpl<DataRowResult> implements ActionName {
    private List<DataRow<Cell>> modifiedRows;
    private FormData formData;
    private int sessionId;

    public List<DataRow<Cell>> getModifiedRows() {
        return modifiedRows;
    }

    public void setModifiedRows(List<DataRow<Cell>> modifiedRows) {
        this.modifiedRows = modifiedRows;
    }

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
	public String getName() {
		return "";
	}
}
