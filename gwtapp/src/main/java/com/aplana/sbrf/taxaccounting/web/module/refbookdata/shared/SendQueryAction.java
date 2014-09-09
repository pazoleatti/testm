package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class SendQueryAction extends UnsecuredActionImpl<SendQueryResult> implements ActionName {

    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
	public String getName() {
		return "Отправить запрос на изменение справочника \"Организации-участники контролируемых сделок\"";
	}
}
