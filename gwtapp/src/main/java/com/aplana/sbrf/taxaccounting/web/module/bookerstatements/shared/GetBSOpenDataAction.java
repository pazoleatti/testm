package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   @author Dmitriy Levykin
 */
public class GetBSOpenDataAction extends UnsecuredActionImpl<GetBSOpenDataResult> implements ActionName {
    @Override
    public String getName() {
        return "Получение начальных данных";
    }
}
