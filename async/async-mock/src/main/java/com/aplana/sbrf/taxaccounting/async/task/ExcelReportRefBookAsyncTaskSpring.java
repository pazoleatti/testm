package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Формирование Excel отчета справочника" для вызова из дев-мода
 * @author lhaziev
 */
@Component("ExcelReportRefBookAsyncTaskSpring")
@Transactional
public class ExcelReportRefBookAsyncTaskSpring extends ExcelReportRefBookAsyncTask {
}
