package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class PrepareSubreportAction {

    private long declarationDataId;
    private TaxType taxType;
    private String type;
    private Map<String, Object> subreportParamValues;

}
