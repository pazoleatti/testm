package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author auldanov
 */
public class GetDestanationPopupDataAction extends UnsecuredActionImpl<GetDestanationPopupDataResult> implements ActionName {

    @Override
    public String getName() {
        return "Получение данных для заполнения модального окна назначений форм подразделениям";
    }
}
