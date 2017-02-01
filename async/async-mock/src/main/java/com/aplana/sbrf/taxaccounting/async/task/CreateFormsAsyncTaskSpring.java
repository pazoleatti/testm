package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Создание экземпляров форм" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("CreateFormsAsyncTaskSpring")
@Transactional
public class CreateFormsAsyncTaskSpring extends CreateFormsAsyncTask {
}