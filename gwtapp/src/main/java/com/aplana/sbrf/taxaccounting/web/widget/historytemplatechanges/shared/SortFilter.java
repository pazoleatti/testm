package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared;

import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;

import java.io.Serializable;

/**
 * @author Fail Mukhametdinov
 */
public class SortFilter implements Serializable {
    private static final long serialVersionUID = 3785744944257435746L;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    /* Столбец, по которому сортируем */
    private VersionHistorySearchOrdering searchOrdering;

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public VersionHistorySearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(VersionHistorySearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }
}
