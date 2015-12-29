package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Формирование CSV отчета справочника" для вызова из дев-мода
 * @author lhaziev
 */
@Component("CSVReportRefBookAsyncTaskSpring")
@Transactional
public class CSVReportRefBookAsyncTaskSpring extends CSVReportRefBookAsyncTask {
}
