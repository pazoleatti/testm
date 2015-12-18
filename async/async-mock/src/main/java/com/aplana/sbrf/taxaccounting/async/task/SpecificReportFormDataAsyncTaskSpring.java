package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Формирование специфичного отчета налоговой формы" для вызова из дев-мода
 * @author lhaziev
 */
@Component("SpecificReportFormDataAsyncTaskSpring")
@Transactional
public class SpecificReportFormDataAsyncTaskSpring extends SpecificReportFormDataAsyncTask {
}
