package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.gwtplatform.dispatch.shared.Result;


public class GetRefBookTreeValuesResult implements Result {
    private static final long serialVersionUID = 1099858233534060155L;

    private PagingResult<RefBookTreeItem> page;

    public PagingResult<RefBookTreeItem> getPage() {
        return page;
    }

    public void setPage(PagingResult<RefBookTreeItem> page) {
        this.page = page;
    }

}
