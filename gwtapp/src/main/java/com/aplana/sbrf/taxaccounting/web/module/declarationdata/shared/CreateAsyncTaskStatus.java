package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

/**
 * Created by lhaziev on 22.05.2015.
 */
public enum CreateAsyncTaskStatus {
    NOT_EXIST_XML, //не существует XML
    EXIST, //существует/задача успешно завершена
    LOCKED, //есть блокировка
    EXIST_TASK, //существуют задачи, которые будут удалены при выполнении данной
    CREATE //создана новая задача
}
