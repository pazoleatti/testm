package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Обновление формы.
 *
 * @author lhaziev
 */
public class RefreshDataRowsAction extends UnsecuredActionImpl<TaskFormDataResult> implements ActionName {

    private boolean force;
    private boolean cancelTask;

    private List<DataRow<Cell>> modifiedRows;
    private FormData formData;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

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

    public boolean isCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(boolean cancelTask) {
        this.cancelTask = cancelTask;
    }

    @Override
	public String getName() {
		return "";
	}
}
