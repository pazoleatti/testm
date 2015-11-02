package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ScriptExposed
public interface DepartmentReportPeriodService {
    DepartmentReportPeriod get(int id);

    Map<Integer, List<Date>> getCorrectionDateListByReportPeriod(Collection<Integer> reportPeriodIds);
}
