package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.Set;

/**
 * Преобразование периода назначения в интервал отчетных периодов
 * @author dloshkarev
 */
public class GetPeriodIntervalAction extends UnsecuredActionImpl<GetPeriodIntervalResult> implements ActionName {
    private Set<CurrentAssign> currentAssigns;
    private TaxType taxType;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Set<CurrentAssign> getCurrentAssigns() {
        return currentAssigns;
    }

    public void setCurrentAssigns(Set<CurrentAssign> currentAssigns) {
        this.currentAssigns = currentAssigns;
    }

    @Override
    public String getName() {
        return "Преобразование периода";
    }
}
