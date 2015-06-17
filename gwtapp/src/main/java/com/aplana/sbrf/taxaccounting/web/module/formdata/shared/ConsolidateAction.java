package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class ConsolidateAction extends UnsecuredActionImpl<ConsolidateResult> {
    private long formDataId;
    private TaxType taxType;
    private boolean manual;
    private boolean force;
    private boolean cancelTask;

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(boolean cancelTask) {
        this.cancelTask = cancelTask;
    }
}
