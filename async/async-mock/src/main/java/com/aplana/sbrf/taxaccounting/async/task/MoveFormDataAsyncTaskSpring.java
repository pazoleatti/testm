package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Подготовка/утверждение/принятие НФ" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("MoveFormDataAsyncTaskSpring")
@Transactional
public class MoveFormDataAsyncTaskSpring extends MoveFormDataAsyncTask {
}