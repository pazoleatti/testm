package com.aplana.sbrf.taxaccounting.controller.form;

import com.aplana.sbrf.taxaccounting.model.Form;

/**
 * Bean, содержащий информацию о редактируемом определении налоговой формы
 */
public class EditFormBean {
	private Form form;

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}
}
