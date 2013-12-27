package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class TemplateFilter implements Serializable {
    private static final long serialVersionUID = 3549128515346222523L;

    TaxType taxType;
    boolean active;

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
}
