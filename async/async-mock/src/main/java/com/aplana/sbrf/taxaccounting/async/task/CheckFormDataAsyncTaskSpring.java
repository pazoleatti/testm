package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Проверка НФ" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("CheckFormDataAsyncTaskSpring")
@Transactional
public class CheckFormDataAsyncTaskSpring extends CheckFormDataAsyncTask {
}