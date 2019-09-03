package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RnuNdflAllPersonsReportFilter {
    private NdflPersonFilter ndflPersonFilter;
    private NdflPersonIncomeDTO personIncome;
    private NdflPersonDeductionDTO ndflPersonDeductionDTO;
    private NdflPersonPrepaymentDTO ndflPersonPrepaymentDTO;
}
