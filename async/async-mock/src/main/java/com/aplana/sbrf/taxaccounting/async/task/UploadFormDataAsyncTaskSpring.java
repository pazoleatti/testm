package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Импорт XLSX-файла" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("UploadFormDataAsyncTaskSpring")
@Transactional
public class UploadFormDataAsyncTaskSpring extends UploadFormDataAsyncTask {
}