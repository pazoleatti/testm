package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Формирование специфичного отчета декларации" для вызова из дев-мода
 * @author lhaziev
 */
@Component("SpecificReportDeclarationDataAsyncTaskSpring")
@Transactional
public class SpecificReportDeclarationDataAsyncTaskSpring extends SpecificReportDeclarationDataGeneratorAsyncTask {
}
