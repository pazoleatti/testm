package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получения типа задачи для НФ
 *
 * @author lhaziev
 */
public class TimerTaskAction extends UnsecuredActionImpl<TimerTaskResult> implements ActionName {

    private long formDataId;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    @Override
	public String getName() {
		return "";
	}
}
