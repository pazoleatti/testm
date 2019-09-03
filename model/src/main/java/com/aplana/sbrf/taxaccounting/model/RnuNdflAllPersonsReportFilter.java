package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class RnuNdflAllPersonsReportFilter implements Serializable {
    private NdflPersonFilter person;
    private NdflPersonIncomeDTO income;
    private NdflPersonDeductionDTO deduction;
    private NdflPersonPrepaymentDTO prepayment;
}
