package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author lhaziev
 */
public class GetReportPeriodsAction extends UnsecuredActionImpl<GetReportPeriodsResult> implements ActionName {

    @Override
    public String getName() {
        return "Получение периодов";
    }
}
