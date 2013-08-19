package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Dmitriy Levykin
 */
public class GetUserDepartmentAction extends UnsecuredActionImpl<GetUserDepartmentResult> implements ActionName {
    @Override
    public String getName() {
        return "Получение начальных данных";
    }
}