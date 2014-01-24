package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение дерева подразделений по определенной выборке
 * @author dloshkarev
 */
public class GetDepartmentTreeAction extends UnsecuredActionImpl<GetDepartmentTreeResult> {
    private FormData formData;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
