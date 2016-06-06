package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Загрузка данных из файла в справочник" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("UploadRefBookAsyncTaskSpring")
@Transactional
public class UploadRefBookAsyncTaskSpring extends UploadRefBookAsyncTask {
}