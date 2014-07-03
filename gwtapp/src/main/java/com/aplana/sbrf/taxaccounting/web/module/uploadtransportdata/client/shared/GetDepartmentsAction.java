package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentsAction extends UnsecuredActionImpl<GetDepartmentsResult> implements ActionName {
    @Override
    public String getName() {
        return "Получение списка подразделений";
    }
}
