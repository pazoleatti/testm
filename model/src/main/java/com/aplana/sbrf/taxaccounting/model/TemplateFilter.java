package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class TemplateFilter implements Serializable {
    private static final long serialVersionUID = 3549128515346222523L;

    TaxType taxType;
    boolean active;
    String searchText;

    public String getSearchText() {
        return searchText != null ? searchText : "";
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateFilter filter = (TemplateFilter) o;

        return active == filter.active && taxType == filter.taxType;
    }

	@Override
	public int hashCode() {
		int result = taxType != null ? taxType.hashCode() : 0;
		result = 31 * result + (active ? 1 : 0);
		return result;
	}
}
