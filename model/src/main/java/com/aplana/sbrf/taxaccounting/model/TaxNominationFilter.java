package com.aplana.sbrf.taxaccounting.model;

/**
 * @author Fail Mukhametdinov
 */
public class TaxNominationFilter {
    private TaxNominationColumnEnum sortColumn;
    private boolean isAscSorting;

    public TaxNominationColumnEnum getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(TaxNominationColumnEnum sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAscSorting() {
        return isAscSorting;
    }

    public void setAscSorting(boolean isAscSorting) {
        this.isAscSorting = isAscSorting;
    }
}
