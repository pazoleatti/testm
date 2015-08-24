package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Проверка формы.
 *
 * @author Eugene Stetsenko
 */
public class CheckFormDataAction extends UnsecuredActionImpl<TaskFormDataResult> implements ActionName {
    private boolean editMode;
    private List<DataRow<Cell>> modifiedRows;
    private FormData formData;
    private boolean manual;
    private boolean force;

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
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

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
	public String getName() {
		return "";
	}
}
