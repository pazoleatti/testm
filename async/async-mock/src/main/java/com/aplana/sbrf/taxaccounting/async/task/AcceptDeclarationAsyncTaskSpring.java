package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Проверка декларации" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("AcceptDeclarationAsyncTaskSpring")
@Transactional
public class AcceptDeclarationAsyncTaskSpring extends AcceptDeclarationAsyncTask {
}