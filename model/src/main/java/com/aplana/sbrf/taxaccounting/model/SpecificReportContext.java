package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class SpecificReportContext {
    private Logger logger;
    private DeclarationData declarationData;
    private DeclarationReportType ddReportType;
    private Map<String, Object> subreportParamValues;
    private Map<String, String> viewParamValues;
    private DataRow<Cell> selectedRecord;
    private RnuNdflAllPersonsReportFilter searchFilter;
    private RnuNdflAllPersonsReportSelectedRows selectedRows;
    private TAUserInfo userInfo;
}
