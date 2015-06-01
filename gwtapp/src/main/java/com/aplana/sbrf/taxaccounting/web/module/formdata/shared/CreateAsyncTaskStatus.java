package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

/**
 * Created by lhaziev on 22.05.2015.
 */
public enum CreateAsyncTaskStatus {
    EXIST, //существует/задача успешно завершена
    LOCKED, //есть блокировка
    CREATE //создана новая задача
}
