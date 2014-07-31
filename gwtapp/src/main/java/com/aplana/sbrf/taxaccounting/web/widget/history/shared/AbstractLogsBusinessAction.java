package com.aplana.sbrf.taxaccounting.web.widget.history.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Fail Mukhametdinov
 */
public abstract class AbstractLogsBusinessAction extends UnsecuredActionImpl<GetLogsBusinessResult> {
    private Long id;
    private SortFilter filter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SortFilter getFilter() {
        return filter;
    }

    public void setFilter(SortFilter filter) {
        this.filter = filter;
    }
}
