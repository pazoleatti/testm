package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Формирование отчетности" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("CreateReportsAsyncTaskSpring")
@Transactional
public class CreateReportsAsyncTaskSpring extends CreateReportsAsyncTask {
}