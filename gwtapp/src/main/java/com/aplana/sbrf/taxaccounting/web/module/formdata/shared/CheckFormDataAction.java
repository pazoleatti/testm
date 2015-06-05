package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Проверка формы.
 *
 * @author Eugene Stetsenko
 */
public class CheckFormDataAction extends AbstractDataRowAction implements ActionName {
    private boolean editMode;

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    @Override
	public String getName() {
		return "Обработка запроса на проверку формы";
	}
}
