package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class RnuNdflAllPersonsReportSelectedRows implements Serializable {
    private List<NdflPersonFilter> persons;
    private List<NdflPersonIncomeDTO> incomes;
    private List<NdflPersonDeductionDTO> deductions;
    private List<NdflPersonPrepaymentDTO> prepayments;
}
