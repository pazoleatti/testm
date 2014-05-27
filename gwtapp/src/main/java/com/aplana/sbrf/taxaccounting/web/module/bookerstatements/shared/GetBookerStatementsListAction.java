package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsFilter;
import com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * @author lhaziev
 */
public class GetBookerStatementsListAction extends UnsecuredActionImpl<GetBookerStatementsListResult> implements ActionName {
    BookerStatementsFilter filter;

    public BookerStatementsFilter getFilter() {
        return filter;
    }

    public void setFilter(BookerStatementsFilter filter) {
        this.filter = filter;
    }

    @Override
    public String getName() {
        return "Получение списка бух. отчетностей";
    }
}
