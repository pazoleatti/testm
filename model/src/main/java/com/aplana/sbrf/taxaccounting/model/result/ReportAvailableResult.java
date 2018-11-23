package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportAvailableResult {
    boolean downloadXmlAvailable;
    boolean downloadXlsxAvailable;
    boolean downloadRnuNdflPersonAllDb;
    boolean downloadKarmannikovaRateReportAvailable;
    boolean downloadReportKppOktmo;
    boolean downloadExcelTemplateAvailable;
    boolean declarationDataExist = true;
}
