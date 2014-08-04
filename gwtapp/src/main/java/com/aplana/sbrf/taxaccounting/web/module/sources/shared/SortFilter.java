package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.SourcesSearchOrdering;

import java.io.Serializable;

/**
 * @author Fail Mukhametdinov
 */
public class SortFilter implements Serializable {
    private static final long serialVersionUID = 8229596653564982939L;
    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    /* Столбец, по которому сортируем */
    private SourcesSearchOrdering searchOrdering;

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public SourcesSearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(SourcesSearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }
}
