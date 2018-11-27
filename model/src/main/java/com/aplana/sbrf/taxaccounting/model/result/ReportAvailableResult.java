package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ReportAvailableResult {
    Map<String, Boolean> reportAvailable = new HashMap<>();
    boolean declarationDataExist = true;

    public void setReportAvailable(String report, boolean available) {
        reportAvailable.put(report, available);
    }
}
