package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetHistoryBusinessFilterAction extends UnsecuredActionImpl<GetHistoryBusinessFilterResult> implements ActionName {
    @Override
    public String getName() {
        return "ПОлучение истории по налогам.";
    }
}
