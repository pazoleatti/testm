package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис по работе с кварталами
 *
 * @author auldanov
 * Date: 03.06.13
 * Time: 14:06
 */
@ScriptExposed
public interface QuarterService {
    /**
     * Возвращает предыдущий отчетный период в данном налоговом периоде
     * если ее нет то возвращается null
     * @return
     */
    ReportPeriod getPrevReportPeriod(int reportPeriodId);
}
