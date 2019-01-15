package com.aplana.sbrf.taxaccounting.model.action;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter @Setter @ToString
public class CreateReportAction {
    private long declarationDataId;
    private String type;
    private Map<String, Object> subreportParamValues;
    private Map<String, Object> selectedRow;

}
