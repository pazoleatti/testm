package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ScriptExposed
public interface DepartmentReportPeriodService {
    DepartmentReportPeriod get(int id);

    Map<Integer, List<Date>> getCorrectionDateListByReportPeriod(Collection<Integer> reportPeriodIds);

    List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Предпоследний отчетный период подразделения для комбинации отчетный период-подразделение
     */
    DepartmentReportPeriod getPrevLast(int departmentId, int reportPeriodId);

    /**
     * Найти id отчетных периодов подразделений для определенного типа подразделения и отчетного периода
     * @param departmentTypeCode
     * @param reportPeriodId
     * @return
     */
    List<Integer> getIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int reportPeriodId);
}
