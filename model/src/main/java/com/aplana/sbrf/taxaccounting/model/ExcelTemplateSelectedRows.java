package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class ExcelTemplateSelectedRows implements Serializable {
    private List<NdflPersonFilter> persons;
    private List<NdflPersonIncomeDTO> incomes;
}
