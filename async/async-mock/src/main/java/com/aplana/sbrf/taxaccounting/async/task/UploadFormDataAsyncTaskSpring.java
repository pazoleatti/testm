package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Расчет НФ" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("UploadFormDataAsyncTaskSpring")
@Transactional
public class UploadFormDataAsyncTaskSpring extends UploadFormDataAsyncTask {
}