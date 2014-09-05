package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;
/**
 * 
 * @author Eugene Stetsenko
 * Результат запроса для удаления формы.
 *
 */
public class DeleteFormDataResult implements Result{
	private static final long serialVersionUID = 5663096332754887482L;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}