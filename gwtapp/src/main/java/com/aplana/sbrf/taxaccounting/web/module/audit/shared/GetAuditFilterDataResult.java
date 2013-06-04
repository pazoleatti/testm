package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetAuditFilterDataResult implements Result{

    private LogSystemFilterAvailableValues availableValues;
    private List<FormDataKind> formDataKinds;
    private List<TaxType> taxTypes;
    private List<TaxPeriod> taxPeriods;

    public List<FormDataKind> getFormDataKinds() {
        return formDataKinds;
    }

    public void setFormDataKinds(List<FormDataKind> formDataKinds) {
        this.formDataKinds = formDataKinds;
    }

    public List<TaxType> getTaxTypes() {
        return taxTypes;
    }

    public void setTaxTypes(List<TaxType> taxTypes) {
        this.taxTypes = taxTypes;
    }

    public LogSystemFilterAvailableValues getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(LogSystemFilterAvailableValues availableValues) {
        this.availableValues = availableValues;
    }

    public List<TaxPeriod> getTaxPeriods() {
        return taxPeriods;
    }

    public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
        this.taxPeriods = taxPeriods;
    }
}
