package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Консолидация НФ" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("ConsolidateFormDataAsyncTaskSpring")
@Transactional
public class ConsolidateFormDataAsyncTaskSpring extends ConsolidateFormDataAsyncTask {
}