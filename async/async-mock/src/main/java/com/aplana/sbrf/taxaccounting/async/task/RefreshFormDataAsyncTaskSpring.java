package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Обновление формы" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("RefreshFormDataAsyncTaskSpring")
@Transactional
public class RefreshFormDataAsyncTaskSpring extends RefreshFormDataAsyncTask {
}