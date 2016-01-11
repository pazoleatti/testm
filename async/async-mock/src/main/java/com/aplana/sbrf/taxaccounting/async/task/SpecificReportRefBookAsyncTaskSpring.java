package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Формирование специфичного отчета справочника" для вызова из дев-мода
 * @author lhaziev
 */
@Component("SpecificReportRefBookAsyncTaskSpring")
@Transactional
public class SpecificReportRefBookAsyncTaskSpring extends SpecificReportRefBookAsyncTask {
}
