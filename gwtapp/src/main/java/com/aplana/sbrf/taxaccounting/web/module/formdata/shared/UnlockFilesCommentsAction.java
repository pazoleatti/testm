package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UnlockFilesCommentsAction extends UnsecuredActionImpl<UnlockFilesCommentsResult> implements ActionName {

	private long formId;

	public long getFormId() {
		return formId;
	}

	public void setFormId(long formId) {
		this.formId = formId;
	}

    @Override
	public String getName() {
		return "Разблокировка модальной формы \"Файлы и комментарии\"";
	}
}
